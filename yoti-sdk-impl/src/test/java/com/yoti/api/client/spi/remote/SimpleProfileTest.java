package com.yoti.api.client.spi.remote;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yoti.api.client.Attribute;
import com.yoti.api.client.Profile;
import com.yoti.api.client.spi.remote.proto.AttrProto;
import com.yoti.api.client.spi.remote.proto.AttrProto.Anchor;

public class SimpleProfileTest {

    private static final String YOTI_ADMIN_VERIFIER_TYPE = "YOTI_ADMIN";

    private static final String PASSPORT_SOURCE_TYPE = "PASSPORT";

    private static final String DRIVING_LICENCE_SOURCE_TYPE = "DRIVING_LICENCE";

    private static final String GIVEN_NAMES_ATTRIBUTE = "given_names";
    private static final String SOME_KEY = "someKey";
    private static final String STARTS_WITH = "startsWith";
    private static final String STRING_VALUE = "test value";
    private static final Integer INTEGER_VALUE = 1;
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldFailConstructionForNullAttributes() {
        new SimpleProfile(null);
    }

    @Test
    public void is_shouldReturnBooleanValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, TRUE));

        boolean result = profile.is(SOME_KEY, false);

        assertTrue(result);
    }

    @Test
    public void is_shouldReturnDefaultBooleanForNonExistingKey() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        boolean result = profile.is(SOME_KEY, false);

        assertFalse(result);
    }

    @Test
    public void is_shouldReturnDefaultBooleanForMismatchingType() {
        Profile p = new SimpleProfile(asList(SOME_KEY, "String"));

        boolean result = p.is(SOME_KEY, false);

        assertFalse(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void is_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.is(null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAttribute_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.getAttribute(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAttributeTyped_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.getAttribute(null, String.class);
    }

    @Test
    public void getAttribute_shouldReturnStringValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, STRING_VALUE));

        String result = profile.getAttribute(SOME_KEY);

        assertEquals(STRING_VALUE, result);
    }

    @Test
    public void getAttribute_shouldReturnNullValueForNonExistingKey() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        assertNull(profile.getAttribute(SOME_KEY));
        assertNull(profile.getAttribute(SOME_KEY, Integer.class));
    }

    @Test
    public void getAttribute_shouldReturnNullValueForMismatchingType() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        assertNull(profile.getAttribute(SOME_KEY));
        assertNull(profile.getAttribute(SOME_KEY, String.class));
    }

    @Test
    public void getAttribute_shouldReturnIntegerValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        Integer result = profile.getAttribute(SOME_KEY, Integer.class);

        assertEquals(INTEGER_VALUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAttributeStartingWith_shouldThrowExceptionForNullAttributeName() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        profile.findAttributeStartingWith(null, Object.class);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnNullWhenNoMatchingName() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        Integer result = profile.findAttributeStartingWith(STARTS_WITH, Integer.class);

        assertNull(result);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnNullForMismatchingType() {
        Profile profile = new SimpleProfile(asList(STARTS_WITH, INTEGER_VALUE));

        String result = profile.findAttributeStartingWith(STARTS_WITH, String.class);

        assertNull(result);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnValueForMatchingKey() {
        Profile profile = new SimpleProfile(asList(STARTS_WITH + ":restOfKey", INTEGER_VALUE));

        Integer result = profile.findAttributeStartingWith(STARTS_WITH, Integer.class);

        assertEquals(INTEGER_VALUE, result);
    }

    @Test
    public void getAttributeSourcesShouldIncludeDrivingLicence() throws ParseException, IOException {
        AttrProto.Attribute attribute = buildAnchoredAttribute(GIVEN_NAMES_ATTRIBUTE, "A_Given_NAME", TestAnchors.DL_ANCHOR);
        
        Profile profile = new SimpleProfile(singletonList(AttributeConverter.convertAttribute(attribute)));
        
        Set<String> sources = profile.getAttributeObject(GIVEN_NAMES_ATTRIBUTE).getSources();
        assertTrue(sources.contains(DRIVING_LICENCE_SOURCE_TYPE));
    }
    
    @Test
    public void getAttributeSourcesShouldIncludePassport() throws ParseException, IOException {
        AttrProto.Attribute attribute = buildAnchoredAttribute(GIVEN_NAMES_ATTRIBUTE, "A_Given_NAME", TestAnchors.PP_ANCHOR);
        
        Profile profile = new SimpleProfile(singletonList(AttributeConverter.convertAttribute(attribute)));
        
        Set<String> sources = profile.getAttributeObject(GIVEN_NAMES_ATTRIBUTE).getSources();
        assertTrue(sources.contains(PASSPORT_SOURCE_TYPE));
    }
    
    @Test
    public void getAttributeVerifiersShouldIncludeYotiAdmin() throws ParseException, IOException {
        AttrProto.Attribute attribute = buildAnchoredAttribute(GIVEN_NAMES_ATTRIBUTE, "A_Given_NAME", TestAnchors.YOTI_ADMIN_ANCHOR);
        
        Profile profile = new SimpleProfile(singletonList(AttributeConverter.convertAttribute(attribute)));
        
        Set<String> sources = profile.getAttributeObject(GIVEN_NAMES_ATTRIBUTE).getVerifiers();
        assertTrue(sources.contains(YOTI_ADMIN_VERIFIER_TYPE));
    }

    private AttrProto.Attribute buildAnchoredAttribute(String name, String value, String rawAnchor)
            throws InvalidProtocolBufferException {
        Anchor anchor = Anchor.parseFrom(Base64.getDecoder().decode(rawAnchor));
        AttrProto.Attribute attribute = AttrProto.Attribute.newBuilder()
                .setName(name)
                .setValue(ByteString.copyFromUtf8(value))
                .addAnchors(anchor)
                .build();
        return attribute;
    }
    
    
    private List<Attribute> asList(String key, Object o) {
        return singletonList(new Attribute(key, o, null));
    }

}
