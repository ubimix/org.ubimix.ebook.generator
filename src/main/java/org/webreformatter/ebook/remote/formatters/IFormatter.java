package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;

import org.webreformatter.ebook.io.IOutput;

/**
 * @author kotelnikov
 */
public interface IFormatter {

    void writeTo(IOutput output) throws IOException;

}