/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.sl.nodes.call;

import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.sl.SLException;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.parser.SLNodeFactory.Modifier;
import com.oracle.truffle.sl.parser.SLNodeFactory.Modifiers;
import com.oracle.truffle.sl.runtime.SLFunction;

/**
 * The node for function invocation in SL. Since SL has first class functions,
 * the {@link SLFunction target function} can be computed by an arbitrary
 * expression. This node is responsible for evaluating this expression, as well
 * as evaluating the {@link #argumentNodes arguments}. The actual dispatch is
 * then delegated to a chain of {@link SLDispatchNode} that form a polymorphic
 * inline cache.
 */
@NodeInfo(shortName = "invoke")
public final class SLInvokeNode extends SLExpressionNode {

	@Child
	private SLExpressionNode functionNode;
	@Children
	private final SLExpressionNode[] argumentNodes;
	@Child
	private SLDispatchNode dispatchNode;

	public SLInvokeNode(SLExpressionNode functionNode, SLExpressionNode[] argumentNodes) {
		this.functionNode = functionNode;
		this.argumentNodes = argumentNodes;
		this.dispatchNode = SLDispatchNodeGen.create();
	}

	@ExplodeLoop
	@Override
	public Object executeGeneric(VirtualFrame frame) {
		Object function = functionNode.executeGeneric(frame);

		/*
		 * The number of arguments is constant for one invoke node. During
		 * compilation, the loop is unrolled and the execute methods of all
		 * arguments are inlined. This is triggered by the ExplodeLoop
		 * annotation on the method. The compiler assertion below illustrates
		 * that the array length is really constant.
		 */
		CompilerAsserts.compilationConstant(argumentNodes.length);

		Object[] argumentValues = new Object[argumentNodes.length];
		for (int i = 0; i < argumentNodes.length; i++) {
			argumentValues[i] = argumentNodes[i].executeGeneric(frame);
		}

		argumentValues = adjustIfVarArg(function, argumentValues);

		return dispatchNode.executeDispatch(frame, function, argumentValues);
	}

	private Object[] adjustIfVarArg(Object function, Object[] argumentValues) {

		Object[] adjustedArgumentValues;

		if (function instanceof SLFunction)
			adjustedArgumentValues = adjustIfVarArg((SLFunction) function, argumentValues);
		else
			adjustedArgumentValues = argumentValues;

		return adjustedArgumentValues;
	}

	@SuppressWarnings("unchecked")
	private Object[] adjustIfVarArg(SLFunction function, Object[] argumentValues) {
		List<FrameSlot> slots = (List<FrameSlot>) function.getCallTarget().getRootNode().getFrameDescriptor()
				.getSlots();
		int numFormalParameters = (int) slots.stream().filter(slot -> {
			Object info = slot.getInfo();
			boolean isFormalParameter;

			if (info != null && info instanceof Modifiers)
				isFormalParameter = true;
			else
				isFormalParameter = false;

			return isFormalParameter;
		}).count();

		Object[] adjustedArgumentValues;
		
		if ( numFormalParameters != 0 ) {
			int argToParamDelta = argumentValues.length - numFormalParameters;

			if (argToParamDelta < 0)
				throw new SLException("Not enough arguments to invoke the function: " + function.getName());

			Object info = slots.get(numFormalParameters - 1).getInfo();
			Modifiers lastParamModifiers = (Modifiers) info;

			if (lastParamModifiers.getModifiers().contains(Modifier.VARARG)) {
				int numVarArgs = argToParamDelta + 1;
				Object[] varargs = Arrays.copyOfRange(argumentValues, argumentValues.length - numVarArgs,
						argumentValues.length);
				adjustedArgumentValues = Arrays.copyOfRange(argumentValues, 0, argumentValues.length - argToParamDelta);
				adjustedArgumentValues[adjustedArgumentValues.length - 1] = varargs;
			} else {
				adjustedArgumentValues = argumentValues;
			}
		}
		else {
			adjustedArgumentValues = argumentValues;
		}

		return adjustedArgumentValues;
	}

	@Override
	protected boolean isTaggedWith(Class<?> tag) {
		if (tag == StandardTags.CallTag.class) {
			return true;
		}
		return super.isTaggedWith(tag);
	}
}
