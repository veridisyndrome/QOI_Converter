package cs107;

/**
 * Utility class to manipulate arrays.
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @apiNote First Task of the 2022 Mini Project
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils() {
    }

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2) {
        boolean valeursIdentiques = true;
        assert (a1 == null && a2 == null) || (a1 != null && a2 != null);

        if (a1 == null) {
            return valeursIdentiques;
        } else {
            for (int i = 0; i < a1.length; i++) {
                if (a1[i] != a2[i]) {
                    valeursIdentiques = false;
                    break;
                }
            }

            return valeursIdentiques;
        }
    }

    /**
     * Check if the content of both arrays is the same
     *
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2) {
        boolean valeursIdentiques = true;
        assert (a1 == null && a2 == null) || (a1 != null && a2 != null);

        if (a1 == null) {
            return valeursIdentiques;
        }

        assert a1.length == a2.length;

        for (int i = 0; i < a1.length; i++) {
            for (int j = 0; j < a1[i].length; j++) {
                if (a1[i][j] != a2[i][j]) {
                    valeursIdentiques = false;
                    break;
                }
            }
        }

        return valeursIdentiques;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     *
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value) {
        byte[] wrap = new byte[1];
        wrap[0] = value;
        return wrap;
    }

    public static byte deWrap(byte[] value) {
        return value[0];
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes) {
        assert bytes != null && bytes.length == 4;

        int val1 = (bytes[0] & 0xFF) << 24;
        int val2 = (bytes[1] & 0xFF) << 16;
        int val3 = (bytes[2] & 0xFF) << 8;
        int val4 = (bytes[3] & 0xFF);

        return val1 | val2 | val3 | val4;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     *
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value) {
        byte a = (byte) (value >> 24);
        byte b = (byte) (value >> 16 & 0xFF);
        byte c = (byte) (value >> 8 & 0xFF);
        byte d = (byte) (value & 0xFF);

        return new byte[]{a, b, c, d};
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     *
     * @param tabs (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte[] ... tabs) {
        assert tabs != null;
        int nombreElement = 0;
        int indice = 0;

        for (int i = 0; i < tabs.length; i++) {
            for (int j = 0; j < tabs[i].length; j++) {
                nombreElement++;
            }
        }

        byte[] tableauFinal = new byte[nombreElement];

        for (int i = 0; i < tabs.length; i++) {
            for (int j = 0; j < tabs[i].length; j++) {
                tableauFinal[indice] = tabs[i][j];
                indice++;
            }
        }
        return tableauFinal;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     *
     * @param bytes (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte... bytes) {
        assert bytes != null;

        byte[] tab = new byte[bytes.length];

        for(int i = 0; i < bytes.length; i++)
        {
            tab[i] = bytes[i];
        }

        return tab;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     *
     * @param input  (byte[]) - Array to extract from
     * @param start  (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     *                        start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length) {
        assert input != null;
        assert start >= 0;
        assert length >= 0;
        assert (start + length) <= input.length;

        byte[] extracted = new byte[length];

        for(int i = 0; i < length; i++)
        {
            extracted[i] = input[start+i];
        }

        return extracted;

    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     *
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     *                        or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int... sizes) {
        assert input != null && sizes != null;

        int somme = 0;

        for (int i = 0; i < sizes.length; i++) {
            somme += sizes[i];
        }

        assert somme == input.length;

        byte[][] partition = new byte[sizes.length][];

        int indice = 0;

        for (int i = 0; i < sizes.length; i++) {
            partition[i] = new byte[sizes[i]];

            for (int j = 0; j < sizes[i]; j++) {
                partition[i][j] = input[indice];
                indice++;
            }
        }

        return partition;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     *
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input) {
        assert input != null;

        for (int i = 0; i < input.length - 1; i++) {
            assert input[i] != null;
            assert input[i].length == input[i + 1].length;
        }

        int lignes = input.length;
        if(lignes == 0){
            return new byte[][]{};
        }
        int colonnes = input[0].length;

        byte[][] output = new byte[lignes * colonnes][4];
        int indice = 0;

        for (int i = 0; i < lignes; i++) {
            for (int j = 0; j < colonnes; j++) {
                byte[] bytes = ArrayUtils.fromInt(input[i][j]);
                byte temp = bytes[0];
                bytes[0] = bytes[1];
                bytes[1] = bytes[2];
                bytes[2] = bytes[3];
                bytes[3] = temp;

                output[indice] = bytes;
                indice++;

            }
        }

        return output;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     *
     * @param input  (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width  (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     *                        or one of the inner arrays of input is null
     *                        or input's length differs from width * height
     *                        or height is invalid
     *                        or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width) {
        assert ((height >= 0) && (width >= 0));
        assert input != null;
        assert input.length == height * width;

        for (int i = 0; i < input.length; i++) {
            assert input[i] != null;
        }

        for (int i = 0; i < input.length; i++) {
            byte temp = input[i][3];
            input[i][3] = input[i][2];
            input[i][2] = input[i][1];
            input[i][1] = input[i][0];
            input[i][0] = temp;
        }

        int[][] output = new int[height][width];
        int indice = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                output[i][j] = ArrayUtils.toInt(input[indice]);
                indice++;
            }
        }

        return output;
    }

}