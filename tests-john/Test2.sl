function main() {
//	t1();
//	t2();
	t3();
}

function t1() {
	o = newArray( 3 );
	o[ 0 ] = 42;
	o[ 1 ] = new();
	o[ 1 ].x = 43;
	
	println( "o[ 0 ] = " + o[ 0 ] );
	println( "o[ 1 ].x = " + o[ 1 ].x );
	println( "arrayLength( o ) = " + arrayLength( o ) );
}

function t2() {
	o = newArray( 3 );
	o[ 0 ] = 42;
	o[ 1 ] = 43;
	o[ 2 ] = 44;

	i = 0;
	while ( i < arrayLength( o ) ) {
		println( "o[ " + i + " ] = " + o[ i ] );
		i = i + 1;
	}
}

function t3() {
	o = newArray( 3 );
	o[ 0 ] = newArray( 4 );
	o[ 0 ][ 0 ] = 42;
	println( "o[ 0 ][ 0 ] = " + o[ 0 ][ 0 ] );
	println( "arrayLength( o[ 0 ] ) = " + arrayLength( o[ 0 ] ) );
}