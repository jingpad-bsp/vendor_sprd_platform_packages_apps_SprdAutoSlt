package com.sprd.autoslt.fingerprint.microarray;

public interface IFactoryTestImpl {
    int factory_init();
    int factory_exit();
    int spi_test();
    int interrupt_test();
    int deadpixel_test();
    int finger_detect(ISprdFingerDetectListener listener);
}
