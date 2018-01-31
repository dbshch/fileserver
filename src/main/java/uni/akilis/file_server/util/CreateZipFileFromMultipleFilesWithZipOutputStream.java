package uni.akilis.file_server.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 动态创建zip文件
 * @author leo
 *
 */
public class CreateZipFileFromMultipleFilesWithZipOutputStream {
	/**
	 * 从多个文件中创建ZIP
	 * @param zipFile zip文件
	 * @param srcFiles	源文件列表
	 * @return 成功返回true，失败返回false
	 */
	public static boolean createZip(String zipFile, List<String> srcFiles){		
		try {
			
			// create byte buffer
			byte[] buffer = new byte[1024];

			FileOutputStream fos = new FileOutputStream(zipFile);

			ZipOutputStream zos = new ZipOutputStream(fos);
			
			for (String fileUrl: srcFiles) {
				
				File srcFile = new File(fileUrl);

				FileInputStream fis = new FileInputStream(srcFile);

				// begin writing a new ZIP entry, positions the stream to the start of the entry data
				zos.putNextEntry(new ZipEntry(srcFile.getName()));
				
				int length;

				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}

				zos.closeEntry();

				// close the InputStream
				fis.close();
				
			}

			// close the ZipOutputStream
			zos.close();
			return true;
			
		}
		catch (IOException ioe) {
			System.out.println("Error creating zip file: " + ioe);
			return false;
		}
		
	}
}
