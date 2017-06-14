function main() {  
  t7();
} 

function t1()
{
	o = new();
	o.i = 42;
	println( o );
	println( o.i );
}

function t2()
{
	o = 1;
//	o.i = 42;
	println( o );
//	println( o.i );
}

function t3()
{
	o = new();
	o[ "x" ] = 47;
	println( "o[ x ] = " + o.x );
}

function t4()
{
	o = new();
	o[ 1 ] = 48;
	println( "o[ 1 ] = " + o[ 1 ] );
}

function t5()
{
	o = new();
	o.i = 49;
	s = "i";
	println( "o[ s ] = " + o[ s ] );
}

function t6()
{
	f = f1;
	println( "f() = " + f() );
}

function f1()
{
	return 50;
}

function t7()
{
	o[ "x" ] = 42;
	println( "o[ x ] = " + o[ "x" ] );
}

