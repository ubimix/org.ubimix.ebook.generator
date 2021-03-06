/**
 * 
 */
package org.ubimix.ebook.remote.formatters;

import java.io.IOException;

import org.ubimix.ebook.io.IInput;
import org.ubimix.ebook.io.IOutput;
import org.ubimix.ebook.io.InOutUtil;
import org.ubimix.ebook.io.server.StreamToInput;
import org.ubimix.ebook.remote.RemoteResourceLoader.RemoteResource;

/**
 * @author kotelnikov
 */
public class ResourceCopyFormatter implements IFormatter {

    private RemoteResource fResource;

    /**
     * @param resource
     */
    public ResourceCopyFormatter(RemoteResource resource) {
        fResource = resource;
    }

    /**
     * @see org.ubimix.ebook.remote.formatters.IFormatter#writeTo(org.ubimix.ebook.io.IOutput)
     */
    @Override
    public void writeTo(IOutput output) throws IOException {
        if (output == null) {
            return;
        }
        try {
            IInput input = new StreamToInput(fResource.getContent());
            try {
                InOutUtil.copy(input, output);
            } finally {
                input.close();
            }
        } finally {
            output.close();
        }
    }

}
