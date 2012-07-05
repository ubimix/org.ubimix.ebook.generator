/**
 * 
 */
package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;

import org.webreformatter.ebook.io.IInput;
import org.webreformatter.ebook.io.IOutput;
import org.webreformatter.ebook.io.InOutUtil;
import org.webreformatter.ebook.io.server.StreamToInput;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;

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
     * @see org.webreformatter.ebook.remote.formatters.IFormatter#writeTo(org.webreformatter.ebook.io.IOutput)
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
