package com.canoo.dolphin.client.util;

import com.canoo.dolphin.mapping.Property;

public class SingleReferenceModel {

    private Property<SimpleTestModel> referenceProperty;

    public Property<SimpleTestModel> getReferenceProperty() {
        return referenceProperty;
    }
}
