package com.ivanspaeth.util;

// All the required imports.

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



/**
 * Console utility used for converting a Comic Book Archive into a PDF document.
 * @author Ivan Spaeth
 *
 */

public class CBZtoPDF {
	
	/**
	 * Setup and configure the default logger for this object.
	 */
	private static Logger logger = Logger.getLogger("com.ivanspaeth.util.CBZtoPDF");
	
	
	/**
	 * Method for converting a CBZ file to a PDF file.
	 * @param cbzFile The file to be converted into a pdf.
	 * @param pdfFile The PDF file to be saved.
	 * @param overwrite Boolean to prevent files from being overwritten.
	 *
	 */
	public static void convertCBZtoPDF
    (
        File cbzFile,
        File pdfFile,
        Boolean overwrite
    ) throws IOException, COSVisitorException, CBZPDFException
    {
		
		PDDocument pddocument;
		
		// Make sure the CB* document exists before we try to read it.
		if ( !cbzFile.exists() )
		{
			logger.log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" does not exist.");
			throw new IOException();
		}
		// TODO: need to make sure the input CBZ is an actual zip by checking more than file extension.
		else if (!cbzFile.getName().toLowerCase().endsWith( ".cbz" ))
		{
			logger.log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" is not a valid Comic Book Archive name.");
			throw new IOException();
		}
		// TODO: need to make sure the output pdf is an actual pdf by checking more than file extension.
		else if (!pdfFile.getName().toLowerCase().endsWith( ".pdf" ))
		{
			logger.log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" is not a valid PDF file name.");
			throw new IOException();
		}
		// Make sure we can actually read the file.
		else if ( !cbzFile.canRead() )
		{
			logger.log(Level.SEVERE, "File \"" + cbzFile.getAbsolutePath() + "\" can not be read.");
			throw new IOException();
		}
		// Test to see if the overwrite flag is passed and if the file exists or not.
		else if (!overwrite && pdfFile.exists())
		{
			logger.log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" already exists.");
			throw new IOException();
		}
		// If the file exists lets make sure we can write too it.
		else if (pdfFile.exists() && overwrite && !pdfFile.canWrite())
		{
			logger.log(Level.SEVERE, "File \"" + pdfFile.getAbsolutePath() + "\" can not be written too.");
			throw new IOException();
		}
		
		// Attempt to create a PDF file in memories.
		pddocument = new PDDocument();
	
		try {
		
			// Initialize the zip file.
			ZipFile zipFile = new ZipFile(cbzFile.getAbsolutePath());
	
			try {

				Enumeration entries = zipFile.entries();
				
				// Loop through each element.
				while(entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entries.nextElement();
					if(!entry.isDirectory() && entry.getName().toLowerCase().endsWith( ".jpg" )) {
						
						// TODO: need to make sure it's an actual JPG file.
						
						// Read the image file.
						logger.log(Level.INFO, entry.getName() + " extracting from source.");
						Image imageFile = ImageIO.read(zipFile.getInputStream(entry));
		
						// Create a rectangle so we can tell the page how big it is.
						PDRectangle cropBox = new PDRectangle();
						cropBox.setLowerLeftX(0);
						cropBox.setLowerLeftY(imageFile.getHeight(null));
						cropBox.setUpperRightY(0);
						cropBox.setUpperRightX(imageFile.getWidth(null));
						
						// Create the page with the proper dimensions.
						logger.log(Level.INFO, entry.getName() + " creating PDPage to match image.");
						PDPage page = new PDPage(cropBox);
						
						// Add the page to the document.
						pddocument.addPage(page);
						
						// Get the image to plop on the page.
						logger.log(Level.INFO, entry.getName() + " drawing image to PDPage.");
						InputStream image = zipFile.getInputStream(entry);
						PDJpeg ximage = new PDJpeg(pddocument, image);
						// Stream the image onto the page.
						PDPageContentStream contentStream = new PDPageContentStream(pddocument, page, true, true);
						// Draw the image onto the stream.
						contentStream.drawImage( ximage, 0, 0 );
						// close it.
						logger.log(Level.INFO, "Closing streams.");
						contentStream.close();
						image.close();
						
						// TODO: create better logging.
						logger.log(Level.INFO, entry.getName() + " added to PDF.");
						
					}
				}
				// Save the created PDF file.
				logger.log(Level.INFO, "Saving PDF file to \"" + pdfFile.getAbsolutePath() + "\"");
				try {
					pddocument.save(pdfFile.getAbsolutePath());
				}
				catch(Exception e)
				{
					logger.log(Level.SEVERE, "Unable to save PDF file to \"" + pdfFile.getAbsolutePath() + "\"");
					throw new CBZPDFException(e);
				}

			}
			finally
			{
				// make sure the zip file is closed.
				zipFile.close();
			}
			
		}
		finally
		{
			// Make sure the PD Document is closed.
			pddocument.close();
		}
		

	}
	
	
	
	
	
	/**
	 * Console utility used for converting a Comic Book Archive into a PDF document.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) throws COSVisitorException, IOException, CBZPDFException {
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

}
