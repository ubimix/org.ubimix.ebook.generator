package org.ubimix.ebook.remote.formatters;

import java.io.IOException;

import org.ubimix.ebook.io.IOutput;

/**
 * @author kotelnikov
 */
public interface IFormatter {

    void writeTo(IOutput output) throws IOException;

}