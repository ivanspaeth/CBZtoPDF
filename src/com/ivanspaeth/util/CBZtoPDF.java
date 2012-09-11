package com.ivanspaeth.util;


import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;


// TODO: Logging is terribad.  Needs work.



/**
 * Console utility used for converting a Comic Book Archive into a PDF document.
 * @author Ivan
 *
 */




public class CBZtoPDF {
	
	public static void convertCBZtoPDF(File cbzFile, File pdfFile, Boolean overwrite) throws IOException, COSVisitorException {
		
		PDDocument pddocument = null;
		
		// Make sure the CB* document exists before we try to read it.
		if ( !cbzFile.exists() )
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" does not exist.");
			throw new IOException();
		}
		// TODO: need to make sure the input CBZ is an actual zip by checking more than file extension.
		else if (!cbzFile.getName().toLowerCase().endsWith( ".cbz" ))
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" is not a valid Comic Book Archive name.");
			throw new IOException();
		}
		// TODO: need to make sure the input CBZ is an actual zip by checking more than file extension.
		else if (!pdfFile.getName().toLowerCase().endsWith( ".pdf" ))
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" is not a valid PDF file name.");
			throw new IOException();
		}
		// Make sure we can actually read the file.
		else if ( !cbzFile.canRead() )
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" can not be read.");
			throw new IOException();
		}
		// Test to see if the overwrite flag is passed and if the file exists or not.
		else if (!overwrite && pdfFile.exists())
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" already exists.");
			throw new IOException();
		}
		// If the file exists lets make sure we can write too it.
		else if (pdfFile.exists() && overwrite && !pdfFile.canWrite())
		{
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" can not be written too.");
			throw new IOException();
		}
		
		// TODO: need to make sure the output PDF file has the PDF extension.
		
		// Attempt to create a PDF file in memories.
		try {
			pddocument = new PDDocument();
		} catch(IOException e) {
			Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.SEVERE, "Unable to create new PD document.");
			throw new IOException();
		}
		
		// Initialize the zip file.
		ZipFile zipFile = new ZipFile(cbzFile.getAbsolutePath());

		// This should be ok, but WHO REALL KNOWS FOR SURE.
		@SuppressWarnings("rawtypes")
		Enumeration entries = zipFile.entries();
		
		// Loop through each element.
		while(entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)entries.nextElement();
			if(!entry.isDirectory() && entry.getName().toLowerCase().endsWith( ".jpg" )) {
				
				// TODO: need to make sure it's an actual JPG file.
				
				// Read the image file.
				Image imageFile = ImageIO.read(zipFile.getInputStream(entry));

				// Create a rectangle so we can tell the page how big it is.
				PDRectangle cropBox = new PDRectangle();
				cropBox.setLowerLeftX(0);
				cropBox.setLowerLeftY(imageFile.getHeight(null));
				cropBox.setUpperRightY(0);
				cropBox.setUpperRightX(imageFile.getWidth(null));
				
				// remove from memory don't need it anymore.
				imageFile = null;
				
				// Create the page with the proper dimensions.
				PDPage page = new PDPage(cropBox);
				
				// Add the page to the document.
				pddocument.addPage(page);
				
				// Get the image to plop on the page.
				InputStream image = zipFile.getInputStream(entry);
				PDJpeg ximage = new PDJpeg(pddocument, image);
				// Stream the image onto the page.
				PDPageContentStream contentStream = new PDPageContentStream(pddocument, page, true, true);
				// Draw the image onto the stream.
				contentStream.drawImage( ximage, 0, 0 );
				// close it.
				contentStream.close();
				image.close();
				
				// TODO: create better logging.
				Logger.getLogger("com.ivanspaeth.util.CBZtoPDF").log(Level.INFO, entry.getName() + " added to PDF.");
				
			}
		}

		zipFile.close();
		pddocument.save(pdfFile.getAbsolutePath());
		pddocument.close();

	}
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
        {
            if( args.length != 2 )
            {
                System.out.println("usage: CBZtoPDF CBZFILE PDFFILE");
            }
            else
            {
            	// Set the logger to display only severe logs.
            	Logger.getLogger("com").getParent().setLevel(Level.SEVERE);
            	// Create the File objects.
            	File cbzFile = new File(args[0]);
            	File pdfFile = new File(args[1]);
            	// Convert it!
            	CBZtoPDF.convertCBZtoPDF(cbzFile, pdfFile, false);
            }
        }
        catch (Exception e) {}
	}

}
