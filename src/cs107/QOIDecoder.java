package cs107;

import static cs107.Helper.Image;
import static cs107.QOISpecification.*;

/**
 * "Quite Ok Image" Decoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @apiNote Third task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder() {
    }

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     *
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header) {
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;
        assert ArrayUtils.equals(ArrayUtils.extract(header, 0, 4), QOISpecification.QOI_MAGIC);

        byte channels = header[12];
        byte colorSpace = header[13];

        assert channels == QOISpecification.RGB || channels == QOISpecification.RGBA;
        assert colorSpace == QOISpecification.ALL || colorSpace == QOISpecification.sRGB;

        int width = ArrayUtils.toInt(ArrayUtils.extract(header, 4, 4));
        int height = ArrayUtils.toInt(ArrayUtils.extract(header, 8, 4));

        assert width > 0;
        assert height > 0;

        return new int[]{width, height, channels, colorSpace};
    }


    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param alpha    (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) {
        assert buffer != null;
        assert input != null;
        assert position < buffer.length && position >= 0;
        assert idx + 3 <= input.length && idx >= 0;

        for (int i = 0; i < 3; i++) {
            buffer[position][i] = input[idx + i];
        }

        buffer[position][3] = alpha;

        return 3;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param input    (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx      (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {
        assert buffer != null;
        assert input != null;
        assert position < buffer.length && position >= 0;
        assert idx + 4 <= input.length && idx >= 0;

        for (int i = 0; i < 4; i++) {
            buffer[position][i] = input[idx + i];
        }

        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {
        assert previousPixel != null;
        assert previousPixel.length == 4;
        assert (chunk & 0b11000000) == QOISpecification.QOI_OP_DIFF_TAG;

        byte b0 = (byte) (previousPixel[0] + ((0b00110000 & chunk) >> 4) - 2);
        byte b1 = (byte) (previousPixel[1] + ((0b00001100 & chunk) >> 2) - 2);
        byte b2 = (byte) (previousPixel[2] + (0b00000011 & chunk) - 2);
        byte b3 = previousPixel[3];

        return ArrayUtils.concat(b0, b1, b2, b3);
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     *
     * @param previousPixel (byte[]) - The previous pixel
     * @param data          (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {
        assert previousPixel != null && data != null;
        assert previousPixel.length == 4;
        assert (byte) (data[0] & 0b11000000) == QOI_OP_LUMA_TAG;

        byte b0 = (byte) ((0b00111111 & data[0]) - 32);
        byte b1 = (byte) (previousPixel[0] + ((0b11110000 & data[1]) >> 4) - 8);
        byte b2 = (byte) (previousPixel[2] + (0b00001111 & data[1]) - 8);
        byte b3 = previousPixel[3];

        return ArrayUtils.concat((byte) (b1 + b0), (byte) (b0 + previousPixel[1]), (byte) (b2 + b0), b3);
    }

    /**
     * Store the given pixel in the buffer multiple times
     *
     * @param buffer   (byte[][]) - Buffer where to store the pixel
     * @param pixel    (byte[]) - The pixel to store
     * @param chunk    (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
        assert buffer != null;
        assert position < buffer.length && position >= 0;
        assert pixel != null;
        assert pixel.length == 4;
        assert buffer[0].length == 4;

        int count = (chunk & 0b00111111) + 1;

        for (int i = position; i < count + position; i++) {
            buffer[i] = pixel;
        }
        return count - 1;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     *
     * @param data   (byte[]) - Data to decode
     * @param width  (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height) {
        assert data != null;
        assert height > 0 && width > 0;

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];

        byte[][] result = new byte[height * width][4];

        int idx = 0;
        int pos = 0;

        while (idx < data.length) {
            byte tag = data[idx];
            if (pos != 0) {
                previousPixel = result[pos - 1];
            }
            if (tag == QOISpecification.QOI_OP_RGB_TAG) {
                decodeQoiOpRGB(result, data, previousPixel[3], pos, idx + 1);
                idx += 4;
            } else if (tag == QOISpecification.QOI_OP_RGBA_TAG) {
                decodeQoiOpRGBA(result, data, pos, idx + 1);
                idx += 5;
            } else if ((byte) (tag & 0b11000000) == QOI_OP_DIFF_TAG) {
                result[pos] = decodeQoiOpDiff(previousPixel, tag);
                idx++;
            } else if ((byte) (tag & 0b11000000) == QOI_OP_LUMA_TAG) {
                result[pos] = decodeQoiOpLuma(previousPixel, ArrayUtils.extract(data, idx, 2));
                idx += 2;
            } else if ((byte) (tag & 0b11000000) == QOI_OP_RUN_TAG) {
                int count = decodeQoiOpRun(result, previousPixel, tag, pos);
                idx++;
                pos += count;
            } else if ((byte) (tag & 0b11000000) == QOI_OP_INDEX_TAG) {
                int index = tag & 0b00111111;
                result[pos] = hashTable[index];
                idx++;
            }

            pos++;
            hashTable[hash(result[pos - 1])] = result[pos - 1];
        }
        assert pos == width * height;

        return result;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     *
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content) {
        assert content != null;

        int[] header = decodeHeader(ArrayUtils.extract(content, 0, QOISpecification.HEADER_SIZE));
        byte[] channels = ArrayUtils.extract(content, HEADER_SIZE, content.length - HEADER_SIZE - QOI_EOF.length);

        int width = header[0];
        int height = header[1];
        int[][] data = ArrayUtils.channelsToImage(decodeData(channels, width, height), height, width);

        return Helper.generateImage(data, (byte) header[2], (byte) header[3]);
    }

}