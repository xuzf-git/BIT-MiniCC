void array4_4(){
	int A[4][4],B[4][4],i,j;
	Mars_PrintStr("Please Input 16 numbers:\n");
	for(i = 0; i < 4; i++)
		for(j = 0; j < 4; j++)
		{
			A[i][j] = Mars_GetInt();
			B[3-j][i] = A[i][j];
		}
	Mars_PrintStr("Array A:\n");
	for( i = 0; i < 4; i++)
	{
		for(j = 0 ; j < 4; j++)
		{
			Mars_PrintInt(A[i][j]);	
		}
		Mars_PrintStr("\n");
	}
	Mars_PrintStr("Array B:\n");
	for( i = 0; i < 4; i++)
	{
		for(j = 0 ; j < 4; j++)
		{
			Mars_PrintInt(B[i][j]);		
		}
		Mars_PrintStr("\n");
	}

	return ;
}
int main(){
	array4_4();
    return 0;
}