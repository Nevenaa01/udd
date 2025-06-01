package com.example.udd.utils;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Singleton class
public final class PDFExtractor {
    private static PDFExtractor INSTANCE;

    // Invoke initial handler and contexts * Node, the limit for BodyContentHandler - Set -1 (unlimited)
    // Probably better to specify some limit so no buffers are overrun (default 100 000)
    BodyContentHandler handler = new BodyContentHandler(-1);
    Metadata metaData = new Metadata();
    ParseContext parseContext = new ParseContext();
    FileInputStream inputStream;
    PDFParser pdfParser = new PDFParser();

    private PDFExtractor() {
    }

    public static PDFExtractor getInstance(){
        if(INSTANCE == null){
            INSTANCE = new PDFExtractor();
        }

        return INSTANCE;
    }

    // Tries to open and extract file specified by the filepath
    public void importPDF(MultipartFile file) throws FileNotFoundException{
        try{
            String filePath = savePDFFileLocally(file);
            inputStream = new FileInputStream(new File(filePath));

            pdfParser.parse(inputStream, handler, metaData, parseContext);
            System.out.println(getDocumentText());
        } catch (IOException e){
            e.printStackTrace();
        } catch (SAXException e){
            e.printStackTrace();
        } catch (TikaException e){
            e.printStackTrace();
        }
    }

    public String savePDFFileLocally(MultipartFile file) throws IOException{
        String projectRoot = System.getProperty("user.dir");
        Path pdfsDir = Paths.get(projectRoot, "pdfs");
        Files.createDirectories(pdfsDir);  // create folder if it doesn't exist
        Path filePath = pdfsDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(filePath.toFile());

        return filePath.toString();
    }

    public String getDocumentText(){
        return handler.toString();
    }

    public Map<String, String> getMetadata(){
        String[] metadataNames = metaData.names();
        Map<String, String> metamap = new HashMap<>();
        for(String name : metadataNames){
            metamap.put(name, metaData.get(name));
        }

        return metamap;
    }
}
