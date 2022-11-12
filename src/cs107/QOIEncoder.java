package cs107;

import java.util.ArrayList;
import java.util.List;

import static cs107.QOISpecification.*;

/**
 * "Quite Ok Image" Encoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @apiNote Second task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder() {
    }

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     *
     * @param image (Helper.Image) - Image to use
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *                        (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     */
    public static byte[] qoiHeader(Helper.Image image) {
        assert image != null;
        assert image.channels() == QOISpecification.RGB || image.channels() == QOISpecification.RGBA;
        assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;

        byte[] magicNumber = QOISpecification.QOI_MAGIC;
        byte[] imageWidth = ArrayUtils.fromInt(image.data()[0].length);
        byte[] imageHeight = ArrayUtils.fromInt(image.data().length);
        byte[] canalsNumber = ArrayUtils.wrap(image.channels());
        byte[] colorSpace = ArrayUtils.wrap(image.color_space());

        return ArrayUtils.concat(magicNumber, imageWidth, imageHeight, canalsNumber, colorSpace);
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     *
     * @param pixel (byte[]) - The Pixel to encode
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGB(byte[] pixel) {
        assert pixel.length == 4;

        byte b0 = QOISpecification.QOI_OP_RGB_TAG;
        byte b1 = pixel[0];
        byte b2 = pixel[1];
        byte b3 = pixel[2];

        return ArrayUtils.concat(b0, b1, b2, b3);
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     *
     * @param pixel (byte[]) - The pixel to encode
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     * @throws AssertionError if the pixel's length is not 4
     */
    public static byte[] qoiOpRGBA(byte[] pixel) {
        assert pixel.length == 4;

        byte b0 = QOISpecification.QOI_OP_RGBA_TAG;
        byte b1 = pixel[0];
        byte b2 = pixel[1];
        byte b3 = pixel[2];
        byte b4 = pixel[3];

        return ArrayUtils.concat(b0, b1, b2, b3, b4);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     *
     * @param index (byte) - Index of the pixel
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     * @throws AssertionError if the index is outside the range of all possible indices
     */
    public static byte[] qoiOpIndex(byte index) {
        assert index >= 0 && index < 64;

        return ArrayUtils.wrap((byte) (QOI_OP_INDEX_TAG | index));
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpDiff(byte[] diff) {
        assert diff != null;
        assert diff.length == 3;

        for (int i = 0; i < diff.length; i++) {
            assert diff[i] > -3 && diff[i] < 2;
            diff[i] = (byte) (diff[i] + 2);
        }

        byte b0 = (byte) (diff[0] << 4);
        byte b1 = (byte) (diff[1] << 2);
        byte b2 = diff[2];

        return ArrayUtils.wrap((byte) (QOI_OP_DIFF_TAG | b0 | b1 | b2));
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     *
     * @param diff (byte[]) - The difference between 2 pixels
     * @return (byte[]) - Encoding of the given difference
     * @throws AssertionError if diff doesn't respect the constraints
     *                        or diff's length is not 3
     *                        (See the handout for the constraints)
     */
    public static byte[] qoiOpLuma(byte[] diff) {
        assert diff != null;
        assert diff.length == 3;
        assert diff[1] > -33 && diff[1] < 32;
        assert diff[0] - diff[1] > -9 && diff[0] - diff[1] < 8;
        assert diff[2] - diff[1] > -9 && diff[2] - diff[1] < 8;

        byte b0 = (byte) (diff[1] + 32);
        byte b1 = (byte) ((diff[0] - diff[1] + 8) << 4);
        byte b2 = (byte) (diff[2] - diff[1] + 8);

        byte[] encoding1 = ArrayUtils.wrap((byte) (QOI_OP_LUMA_TAG | b0));
        byte[] encoding2 = ArrayUtils.wrap((byte) (b1 | b2));


        return ArrayUtils.concat(encoding1, encoding2);
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     *
     * @param count (byte) - Number of similar pixels
     * @return (byte[]) - Encoding of count
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     */
    public static byte[] qoiOpRun(byte count) {
        assert count > 0 && count < 63;

        return ArrayUtils.wrap((byte) (QOI_OP_RUN_TAG | (count - 1)));
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     *
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image) {
        assert image != null;

        byte[] previousPixel = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];

        int counter = 0;
        List<byte[]> arrayList = new ArrayList<>();

        for (int i = 0; i < image.length; i++) {
            assert image[i] != null;
            assert image[i].length == 4;
        }

        for (int i = 0; i < image.length; i++) {
            byte[] pixel = image[i];
            if (i != 0) {
                previousPixel = image[i-1];
            }

            if (ArrayUtils.equals(pixel, previousPixel)) {
                counter++;
                if (counter == 62 || i == image.length - 1) {
                    arrayList.add(qoiOpRun((byte) counter));
                    counter = 0;
                }
                continue;
            } else {
                if (counter != 0) {
                    arrayList.add(qoiOpRun((byte) counter));
                    counter = 0;
                }
            }

            if (tryHashEncode(hashTable, arrayList, pixel)) continue;

            if (pixel[3] == previousPixel[3]) {
                if (tryDiffEncode(previousPixel, arrayList, pixel)) continue;

                if (tryLumaEncode(arrayList, pixel, previousPixel)) continue;
                arrayList.add(QOIEncoder.qoiOpRGB(pixel));
            } else {
                arrayList.add(qoiOpRGBA(pixel));
            }
        }

        byte[][] newArray = new byte[arrayList.size()][];
        for(int i = 0; i < arrayList.size(); ++i){
           newArray[i] = arrayList.get(i);

        }
        return ArrayUtils.concat(newArray);
    }

    private static boolean tryLumaEncode(List<byte[]> arrayList, byte[] pixel, byte[] previousPixel) {
        byte dr = (byte)(pixel[0] - previousPixel[0]);
        byte dg = (byte)(pixel[1] - previousPixel[1]);
        byte db = (byte)(pixel[2] - previousPixel[2]);

        if ((dg > -33 && dg < 32)
                && (dr - dg > -9 && dr - dg < 8)
                && (db - dg > -9 && db - dg < 8)) {
            arrayList.add(QOIEncoder.qoiOpLuma(ArrayUtils.concat(dr, dg, db)));
            return true;
        }
        return false;
    }

    private static boolean tryDiffEncode(byte[] previousPixel, List<byte[]> arrayList, byte[] pixel) {
        int diffCounter = 0;
        for (int j = 0; j < 3; j++) {
            if (((byte)(pixel[j] - previousPixel[j]) > -3 && (byte)(pixel[j] - previousPixel[j]) < 2)) {
                ++diffCounter;
            }
        }
        if (diffCounter == 3) {
            byte[] delta = {(byte)(pixel[0] - previousPixel[0]), (byte)(pixel[1] - previousPixel[1]), (byte)(pixel[2] - previousPixel[2])};

            arrayList.add(QOIEncoder.qoiOpDiff(delta));
            return true;
        }
        return false;
    }

    private static boolean tryHashEncode(byte[][] hashTable, List<byte[]> arrayList, byte[] pixel) {
        if (ArrayUtils.equals(hashTable[hash(pixel)], pixel)) {
            arrayList.add(qoiOpIndex(hash(pixel)));
            return true;
        } else {
            hashTable[hash(pixel)] = pixel;
        }
        return false;
    }


    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     *
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     */
    public static byte[] qoiFile(Helper.Image image) {
        assert image != null;

        return ArrayUtils.concat(QOIEncoder.qoiHeader(image), QOIEncoder.encodeData(ArrayUtils.imageToChannels(image.data())), QOI_EOF);
    }

}