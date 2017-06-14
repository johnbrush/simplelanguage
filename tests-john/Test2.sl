function main() {
	o = newArray( 3 );
	o[ 0 ] = 42;
	o[ 1 ] = new();
	o[ 1 ].x = 43;
	println( "o[ 0 ] = " + o[ 0 ] );
	println( "o[ 1 ].x = " + o[ 1 ].x );
} 
