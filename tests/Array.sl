function main() {
	testSimpleArray();
}

function testSimpleArray()
{
	a = newArray( 5 );
	a[ 0 ] = 42;				// test integer
	a[ 1 ] = "beeblebrox";		// test string
	a[ 2 ] = new();				// test object
	a[ 2 ].m = 7;				// test member
	a[ 3 ] = newArray( 3 );		// test nested array
	a[ 3 ][ 0 ] = 11;
	a[ 3 ][ 1 ] = 13;
	a[ 3 ][ 2 ] = 17;
	a[ 4 ] = newArray( 0 );		// test array with zero elements
	
	println( "a[ 0 ] = " + a[ 0 ] );
	println( "a[ 1 ] = " + a[ 1 ] );
	println( "a[ 2 ].m = " + a[ 2 ].m );
	println( "a[ 3 ][ 0 ] = " + a[ 3 ][ 0 ] );
	println( "a[ 3 ][ 1 ] = " + a[ 3 ][ 1 ] );
	println( "a[ 3 ][ 2 ] = " + a[ 3 ][ 2 ] );
	
	// test arrayLength() function
	println( "arrayLength( a ) = " + arrayLength( a ) );
	println( "arrayLength( a[ 3 ] ) = " + arrayLength( a[ 3 ] ) );
	println( "arrayLength( a[ 4 ] ) = " + arrayLength( a[ 4 ] ) );
	
	// test varargs
	println( "f( 7 ) = " + f( 7 ) );					// test single argument
	println( "f( 7, 11 ) = " + f( 7, 11 ) );			// test multiple arguments
	println( "f() = " + f() );							// test zero arguments
	println( "f2( 7, 11, 13 ) = " + f2( 7, 11, 13 ) );	// test normal argument preceding varargs
}

function f( ... x )
{
	i = 0;
	sum = 0;
	while ( i < arrayLength( x ) ) {
		sum = sum + x[ i ];
		i = i + 1;
	}
	
	return sum;
}

function f2( x, ... y )
{
	return x + f( y );
}
