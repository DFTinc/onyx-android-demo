package com.dft.onyxandroiddemo.matching;

import android.graphics.Bitmap;

import com.dft.onyx.FingerprintTemplate;

public class VerifyPayload {
    private FingerprintTemplate reference;
    private Bitmap probe;

    public VerifyPayload(FingerprintTemplate reference, Bitmap probe) {
        this.setReference(reference);
        this.setProbe(probe);
    }

    public FingerprintTemplate getReference() {
        return reference;
    }

    public void setReference(FingerprintTemplate reference) {
        this.reference = reference;
    }

    public Bitmap getProbe() {
        return probe;
    }

    public void setProbe(Bitmap probe) {
        this.probe = probe;
    }
}
