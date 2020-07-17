/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package eu.hohenegger.lemminx.extensions.contentmodel.participants.wrapper.copy;

import static org.eclipse.lemminx.utils.StringUtils.getString;
import static eu.hohenegger.lemminx.utils.wrapper.LMXPositionUtility.selectCurrentTagOffset;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xni.QName;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.ElementUnterminatedCodeAction;
import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.EqRequiredInAttributeCodeAction;
import org.eclipse.lemminx.extensions.contentmodel.participants.codeactions.OpenQuoteExpectedCodeAction;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4j.Range;

import eu.hohenegger.lemminx.utils.wrapper.LMXPositionUtility;
import eu.hohenegger.lemminx.xerces.wrapper.LMXLocator;

/**
 * XML error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum LMXSyntaxErrorCode implements IXMLErrorCode {

    AttributeNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNotUnique
    AttributeNSNotUnique, // https://wiki.xmldation.com/Support/Validator/AttributeNSNotUnique
    AttributePrefixUnbound, ContentIllegalInProlog, // https://wiki.xmldation.com/Support/Validator/ContentIllegalInProlog
    DashDashInComment, // https://wiki.xmldation.com/Support/Validator/DashDashInComment
    ElementUnterminated, // https://wiki.xmldation.com/Support/Validator/ElementUnterminated
    ElementPrefixUnbound, // https://wiki.xmldation.com/Support/Validator/ElementPrefixUnbound
    EmptyPrefixedAttName, // https://wiki.xmldation.com/Support/Validator/EmptyPrefixedAttName
    EncodingDeclRequired, // https://wiki.xmldation.com/Support/Validator/EncodingDeclRequired
    ETagRequired, // https://wiki.xmldation.com/Support/Validator/ETagRequired
    ETagUnterminated, // https://wiki.xmldation.com/Support/Validator/ETagUnterminated
    EqRequiredInAttribute, // https://wiki.xmldation.com/Support/Validator/EqRequiredInAttribute
    the_element_type_lmsg("the-element-type-lmsg"), EqRequiredInXMLDecl, IllegalQName, InvalidCommentStart,
    LessthanInAttValue, MarkupEntityMismatch, MarkupNotRecognizedInContent, NameRequiredInReference, OpenQuoteExpected,
    PITargetRequired, PseudoAttrNameExpected, QuoteRequiredInXMLDecl, RootElementTypeMustMatchDoctypedecl,
    SDDeclInvalid, SpaceRequiredBeforeEncodingInXMLDecl, SpaceRequiredBeforeStandalone, SpaceRequiredInPI,
    VersionInfoRequired, VersionNotSupported, XMLDeclUnterminated, CustomETag, PrematureEOF, DoctypeNotAllowed, NoMorePseudoAttributes;

    private final String code;

    private LMXSyntaxErrorCode() {
        this(null);
    }

    private LMXSyntaxErrorCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        if (code == null) {
            return name();
        }
        return code;
    }

    private final static Map<String, LMXSyntaxErrorCode> codes;

    static {
        codes = new HashMap<>();
        for (LMXSyntaxErrorCode errorCode : values()) {
            codes.put(errorCode.getCode(), errorCode);
        }
    }

    public static LMXSyntaxErrorCode get(String name) {
        return codes.get(name);
    }

    /**
     * Create the LSP range from the SAX error.
     * 
     * @param location
     * @param key
     * @param arguments
     * @param document
     * @return the LSP range from the SAX error.
     */
    public static Range toLSPRange(LMXLocator location, LMXSyntaxErrorCode code, Object[] arguments,
            DOMDocument document) {
        int offset = location.getCharacterOffset() - 1;
        // adjust positions
        switch (code) {
        case SpaceRequiredBeforeStandalone:
        case SpaceRequiredBeforeEncodingInXMLDecl:
        case VersionInfoRequired:
        case ElementPrefixUnbound:
        case ElementUnterminated:
        case RootElementTypeMustMatchDoctypedecl:
            return LMXPositionUtility.selectStartTagName(offset, document);
        case EqRequiredInAttribute: {
            String attrName = getString(arguments[1]);
            return LMXPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
        }
        case NoMorePseudoAttributes:
        case EncodingDeclRequired:
        case EqRequiredInXMLDecl:
            return LMXPositionUtility.selectAttributeNameAt(offset, document);
        case AttributeNSNotUnique: {
            String attrName = getString(arguments[1]);
            Range xmlns = LMXPositionUtility.selectAttributeNameFromGivenNameAt("xmlns:" + attrName, offset, document);
            if (xmlns != null) {
                return xmlns;
            }
            return LMXPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
        }
        case AttributeNotUnique: {
            String attrName = getString(arguments[1]);
            return LMXPositionUtility.selectAttributeNameFromGivenNameAt(attrName, offset, document);
        }
        case AttributePrefixUnbound: {
            return LMXPositionUtility.selectAttributePrefixFromGivenNameAt(getString(arguments[1]), offset, document);
        }
        case LessthanInAttValue: {
            String attrName = getString(arguments[1]);
            return LMXPositionUtility.selectAttributeValueAt(attrName, offset, document);
        }
        case QuoteRequiredInXMLDecl: {
            String attrName = getString(arguments[0]);
            return LMXPositionUtility.selectAttributeValueAt(attrName, offset, document);
        }
        case EmptyPrefixedAttName: {
            QName qName = (QName) arguments[0];
            return LMXPositionUtility.selectAttributeValueAt(qName.rawname, offset, document);
        }
        case SDDeclInvalid:
        case VersionNotSupported: {
            String attrValue = getString(arguments[0]);
            return LMXPositionUtility.selectAttributeValueByGivenValueAt(attrValue, offset, document);
        }
        case ETagUnterminated:
            /**
             * Cases:
             * 
             * <a> </b>
             * 
             * <a> <b> </b> </c>
             * 
             * <a> <a> </a> </b
             */
            return LMXPositionUtility.selectPreviousNodesEndTag(offset, document);
        case CustomETag:
            return LMXPositionUtility.selectEndTagName(offset, document);
        case ETagRequired: {
            String tag = getString(arguments[0]);
            return LMXPositionUtility.selectChildEndTag(tag, offset, document);
        }
        case ContentIllegalInProlog: {
            int startOffset = offset + 1;
            int endOffset = 0;
            int errorOffset = offset + 1;
            String text = document.getText();
            int startPrologOffset = text.indexOf("<");
            if (errorOffset < startPrologOffset) {
                // Invalid content given before prolog. Prolog should be the first thing in the
                // file if given.
                startOffset = errorOffset;
                endOffset = startPrologOffset;
            } else {
                // Invalid content given after prolog. Either root tag or comment should be
                // present
                int firstStartTagOffset = text.indexOf("<", errorOffset);
                startOffset = errorOffset;
                endOffset = firstStartTagOffset != -1 ? firstStartTagOffset : text.length();
            }
            return LMXPositionUtility.createRange(startOffset, endOffset, document);
        }
        case DashDashInComment: {
            int endOffset = offset + 1;
            int startOffset = offset - 1;
            return LMXPositionUtility.createRange(startOffset, endOffset, document);
        }
        case IllegalQName:
        case InvalidCommentStart:
        case MarkupNotRecognizedInContent:
            return LMXPositionUtility.createRange(offset, offset + 1, document);
        case MarkupEntityMismatch:
            return LMXPositionUtility.selectRootStartTag(document);
        case NameRequiredInReference:
            break;
        case OpenQuoteExpected: {
            return LMXPositionUtility.selectAttributeNameAt(offset - 1, document);
        }
        case DoctypeNotAllowed:
            DOMDocumentType docType = document.getDoctype();
            return LMXPositionUtility.createRange(docType);        
        case PITargetRequired:
            // Working
            break;
        case PseudoAttrNameExpected:
            // Working
            // Add better message
            break;
        case SpaceRequiredInPI:
            int start = selectCurrentTagOffset(offset, document) + 1;
            int end = offset + 1;
            return LMXPositionUtility.createRange(start, end, document);
        case PrematureEOF:
        case XMLDeclUnterminated:
            break;
        default:
        }

        return null;

    }

    public static void registerCodeActionParticipants(Map<String, ICodeActionParticipant> codeActions) {
        codeActions.put(ElementUnterminated.getCode(), new ElementUnterminatedCodeAction());
        codeActions.put(EqRequiredInAttribute.getCode(), new EqRequiredInAttributeCodeAction());
        codeActions.put(OpenQuoteExpected.getCode(), new OpenQuoteExpectedCodeAction());
    }
}
