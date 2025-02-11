package com.license4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExampleTest {

    /**
     * "Example Product 1" hash value.
     */
    private static final String PRODUCT_HASH1 = "11DE9AB9EF29E3CA2B68521D4AACC2A7";

    /**
     * "Example Product 2" hash value. This product has been disabled on the
     * license server to test below.
     */
    private static final String PRODUCT_HASH2 = "22DA9CD9EF29E3C04551021A4AA7D2B6";

    /**
     * A valid node-locked type license.
     */
    private static final String KEY_VALID_NODELOCKED = "12345-67890-ABCDE-12345";

    /**
     * A valid floating type license.
     */
    private static final String KEY_VALID_FLOATING = "54321-67890-ABCDE-12345";

    /**
     * This license key has expired.
     */
    private static final String KEY_EXPIRED = "78912-67890-ABCDE-12345";

    /**
     * There are existing validations for this license on the server, and it has
     * exceeded the maximum allowed usage limit.
     */
    private static final String KEY_MAX_USAGE_REACHED = "85296-67890-ABCDE-12345";

    /**
     * This license has the following features defined on the license server.
     * There are two feature related to core (one is enough actually) to test
     * features below.
     *
     * <pre>
     * username=abcuser
     * my-product-edition=Professional
     * AnyFeatureKey=anything1
     * my-product-core-1=1
     * my-product-core-32=32
     * my-product-version=1.99
     * </pre>
     */
    private static final String KEY_FEATURES = "22336-78900-QWERT-12345";

    /**
     * This license is disabled on the server.
     */
    private static final String KEY_DISABLED = "ABCDE-12345-QWERT-82821";

    /**
     * The product associated with this license is disabled on the license
     * server.
     */
    private static final String KEY_VALID_PRODUCT_DISABLED = "MNBVC-12345-81021-81801";

    @Test
    @DisplayName("Default License File Location Test")
    void file01() {
        String defaultLicenseFileLocation
                = System.getProperty("user.home").replaceAll("\\\\", "/") // user home (replace \\ with / on Windows)
                + "/." + PRODUCT_HASH1.substring(PRODUCT_HASH1.length() - 8) // dot and last 8 characters of hash value
                + "/license.l4j"; // default license file name

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        Assertions.assertEquals(defaultLicenseFileLocation, License.getInstance().getLicenseInformation().getLicenseDataSaveLocation());
    }

    @Test
    @DisplayName("Custom License File Location Test")
    void file02() {
        String customLicenseFileLocation = System.getProperty("user.home").replaceAll("\\\\", "/") + "/.MyExampleProduct1/license.lic";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .file(customLicenseFileLocation)
                .build();

        Assertions.assertEquals(customLicenseFileLocation, License.getInstance().getLicenseInformation().getLicenseDataSaveLocation());
    }

    @Test
    @DisplayName("License Registory Location Test")
    void registry01() {
        if (System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows")) { // Windows
            String regKey = "MyExampleProduct1"; // registry key
            String regValue = "license"; // registry value, binary

            License.getInstance().getBuilder()
                    .product(PRODUCT_HASH1)
                    .registry(regKey, regValue)
                    .build();

            Assertions.assertEquals("Computer\\HKEY_CURRENT_USER\\SOFTWARE\\" + regKey + "\\" + regValue, License.getInstance().getLicenseInformation().getLicenseDataSaveLocation());
        } else { // Linux, MAC. No registry.
            Assertions.assertTrue(true);
        }
    }

    @Test
    @DisplayName("Invalid Characters in License Key Test")
    void key01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate("ABCDE 12345"); // Allowed characters: A-Z 0-9 and - (space is not allowed)

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(1005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Very Short License Key Test")
    void key02() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate("ABCDE"); // minimum length 10 characters

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(1005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Very Long License Key Test")
    void key03() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        // maximum length 255 characters
        License.getInstance().validate("1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890-1234567890");

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(1005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Wihtout Any Feature Given Test")
    void feature01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        // License has some features defined, but none is given to builder, so that there is no features to verify.
        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Any Valid Feature Given Test")
    void feature02() {
        String anyProductFeatureValue = "anything1";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("AnyFeatureKey", anyProductFeatureValue)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(anyProductFeatureValue, License.getInstance().getLicenseInformation().getFeature("AnyFeatureKey").getValue())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Any Invalid Feature Given Test")
    void feature03() {
        String anyProductFeatureValue = "anything2";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("AnyFeatureKey", anyProductFeatureValue)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Non-Existing Features in the License Test")
    void feature04() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("abcde", "something")
                .build();

        License.getInstance().validate(KEY_FEATURES);

        // If any feature given to builder does not exist in the license, than it is ignored, so license becomes valid.
        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Verify Feature After Validation Manually")
    void feature05() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // License validated (activated) and valid, then check for a feature.
        // In this test license, we have a feature with key "AnyFeatureKey" and value "anything1"
        Assertions.assertEquals("anything1", License.getInstance().getLicenseInformation().getFeature("AnyFeatureKey").getValue());

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Valid Product Edition Test")
    void edition01() {
        String myProductEdition = "Professional";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-edition", myProductEdition)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Invalid Product Edition Test")
    void edition02() {
        String myProductEdition = "Standard";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-edition", myProductEdition)
                .build();

        // Defined product edition in the license is Professional, not Standard
        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Valid Product Version Test")
    void version01() {
        String myProductVersion = "1.1";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-version", myProductVersion)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Invalid Product Version Test")
    void version02() {
        String myProductVersion = "2.0";

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-version", myProductVersion)
                .build();

        // Valid max product version defined in license is 1.99 so giving 2.0 make it invalid
        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Valid CPU Core Count Test")
    void core01() {
        int core = License.getInstance().getSystemInformation().getCPUCoreCount();

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-core-32", core)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Invalid CPU Core Count Test")
    void core02() {
        int core = License.getInstance().getSystemInformation().getCPUCoreCount();

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("my-product-core-1", core)
                .build();

        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Invalid OS Username Test")
    void user01() {
        String username = License.getInstance().getSystemInformation().getOSUserName();

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .feature("username", username)
                .build();

        // Defined "username" feature has a value "abcuser", if your username on your OS is not abcuser, license will be invalid.
        License.getInstance().validate(KEY_FEATURES);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3005, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Max Allowed Usage for Node-Locked Reached Test")
    void maxUsage01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        // For testing, this license max allowed usage value is 1, and there is already a validation (activation) exists.
        License.getInstance().validate(KEY_MAX_USAGE_REACHED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3008, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Invalid License Key Test")
    void maxUsage02() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        // If any given license key (valid format and characters) is not found on the license server,
        // it will return as license key not found in the error code/messsage.
        License.getInstance().validate("THIS-IS-AN-INVALID-LICENSE-KEY");

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3006, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("License Disabled on the License Server")
    void disabled01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate(KEY_DISABLED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3007, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Product Disabled on the License Server")
    void disabled02() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH2)
                .build();

        // If product is disabled, all of its licenses will be also invalid.
        License.getInstance().validate(KEY_VALID_PRODUCT_DISABLED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3007, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Expired License")
    void expired01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate(KEY_EXPIRED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3004, License.getInstance().getStatus().getCode())
        );
    }

    @Test
    @DisplayName("Invalidate License")
    void invalidate01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate(KEY_VALID_NODELOCKED);

        // validated
        Assertions.assertTrue(License.getInstance().getStatus().isValid());

        License.getInstance().invalidate();

        // invalidated, and license file removed.
        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertFalse(Files.exists(Paths.get(License.getInstance().getLicenseInformation().getLicenseDataSaveLocation())))
        );
    }

    @Test
    @DisplayName("Validate a Floating License Test")
    void floating01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .build();

        License.getInstance().validate(KEY_VALID_FLOATING);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // no need to invalidate, when JVM exits, it is invalidated. If JVM exits abnormally, license server invalidates dead usage after 30 minutes max.
        // but, it can also be invalidated, to remove the license usage when needed.
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Validate with a Custom Fingerprint 1")
    void customFingerprint01() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .customFingerprint("SOME-GENERATED-FINGERPRINT-UNIQUE-DEVICE-OR-USER-ANYTHING-IDENTIFIER")
                .build();

        // custom fingerprint may be anything you can generate each time product runs. It should be a unique.
        License.getInstance().validate(KEY_VALID_NODELOCKED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Validate with a Custom Fingerprint 2")
    void customFingerprint02() {
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .customFingerprint("A-FINGERPRINT")
                .build();

        License.getInstance().validate(KEY_VALID_NODELOCKED);

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // Later, custom identifier is somehow changed on device. So, license becomes invalid.
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .customFingerprint("B-FINGERPRINT")
                .build();

        // License has already been validated before with A-FINGERPRINT, and saved on default license file,
        // so just calling vaildate to simulate normal software running.
        License.getInstance().validate();

        Assertions.assertAll(
                "",
                () -> Assertions.assertFalse(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(3003, License.getInstance().getStatus().getCode())
        );

        // again back to valid fingerprint which is A-FINGERPRINT
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .customFingerprint("A-FINGERPRINT")
                .build();

        // License has already been validated before with A-FINGERPRINT, and saved on default license file,
        // so just calling vaildate to simulate normal software running.
        License.getInstance().validate();

        Assertions.assertAll(
                "",
                () -> Assertions.assertTrue(License.getInstance().getStatus().isValid()),
                () -> Assertions.assertEquals(0, License.getInstance().getStatus().getCode())
        );

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    /*
    Uncomment the following test after replacing USB stick vendorId and productId with your own.
    To use the Licensing Library to detect vendor id and product id, use the following code.
    License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .usbDongle(null, null, "license.lic") // null vendor and product id, put an empty file named "license.lic" in the root folder.
                .build();
    System.out.println("name            :" + License.getInstance().getSystemInformation().getUSBDongleName());
    System.out.println("vendor Id       :" + License.getInstance().getSystemInformation().getUSBDongleVendorId());
    System.out.println("product Id      :" + License.getInstance().getSystemInformation().getUSBDongleProductId());
    System.out.println("serial          :" + License.getInstance().getSystemInformation().getUSBDongleSerial());
    
    @Test
    @DisplayName("Dongle VID PID found")
    void usb01() {
        String myUSBVendorId = "0951"; // Kingston
        String myUSBProductId = "1625"; // DataTraveler

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .usbDongle(myUSBVendorId, myUSBProductId, "license.lic")
                .build();

        // If there is USB stick with matching vendor Id and product Id found,
        // AND there is file in root folder with name "license.lic" its serial number
        // will be used in validating the license
        License.getInstance().validate(KEY_VALID_NODELOCKED);
        
        Assertions.assertTrue(License.getInstance().getStatus().isValid());

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    @Test
    @DisplayName("Dongle NO VID PID given")
    void usb02() {
        // null for vendor id and product id accepted, but It is better to use a reliable brand of USB stick..
        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .usbDongle(null, null, "license.lic")
                .build();

        // If there is USB stick with matching vendor Id and product Id found,
        // AND there is file in root folder with name "license.lic" its serial number
        // will be used in validating the license
        License.getInstance().validate(KEY_VALID_NODELOCKED);

        Assertions.assertTrue(License.getInstance().getStatus().isValid());

        // invalidate license to remove validation (activation) on the license server and local
        License.getInstance().invalidate();
    }

    // Insert 2 USB sticks in your system, use one to validate license
    // then copy the license file to the other USB stick, license will be invalid.
    @Test
    @DisplayName("Dongle, License File Copied")
    void usb03() {
        String myUSBVendorId = "0951"; // Kingston
        String myUSBProductId = "1625"; // DataTraveler

        License.getInstance().getBuilder()
                .product(PRODUCT_HASH1)
                .usbDongle(myUSBVendorId, myUSBProductId, "license.lic")
                .build();

        // If there is USB stick with matching vendor Id and product Id found,
        // AND there is file in root folder with name "license.lic" its serial number
        // will be used in validating the license
        License.getInstance().validate(KEY_VALID_NODELOCKED);

        Assertions.assertTrue(License.getInstance().getStatus().isValid());

        String originalLocationUSB = License.getInstance().getLicenseInformation().getLicenseDataSaveLocation();
        String invalidTestLocationCopiedUSB = "E:/license.lic";
        try {
            // move the license file to the other USB stick (E:/ drive used as an example below).
            Files.move(Paths.get(originalLocationUSB), Paths.get(invalidTestLocationCopiedUSB), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // validate again, without given license key, it will be invalid
        License.getInstance().validate();

        // If the other USB has same vendor Id and prodıuct Id, error code will be 3003 ("system device fingerprint mismatch")
        // if the other USB is a different brand/model, error code will 1011 ("usb dongle not detected").
        Assertions.assertFalse(License.getInstance().getStatus().isValid());

        try {
            // move back license file to original valid location USB
            Files.move(Paths.get(invalidTestLocationCopiedUSB), Paths.get(originalLocationUSB), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
}
