package kifio.utils

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.awt.Image
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

class SVGTranscoder {

    fun transcodeSVGDocument(file: File): Image {
        // Create a JPEG transcoder
        val t = PNGTranscoder()
        val inputStream = file.toURI().toString()
        val outputStream = file.toURI().toString()

        // Create the transcoder input.
        val input = TranscoderInput(inputStream)
        val output = TranscoderOutput(outputStream)

        // Save the image.
        t.transcode(input, output)

        val data = outputStream.toByteArray()
        return ImageIO.read(ByteArrayInputStream(data))
    }
}