import java.util.Arrays;

public class test {

	public static String twoDimArrayToString(int[][] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < array.length; i++) {
			sb.append(Arrays.toString(array[i]));
			if (i < array.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[][] field = new int[][] { { 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0 } };

		int[] field2 = new int[] { 1, 2, 3 };

		System.out.println(Arrays.toString(field2));
		
		System.out.println(twoDimArrayToString(field));
	}

}
