/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 12.09.2004
 * 
 * @ejb.bean name="FileSystemMgt" type="Stateless" view-type="remote"
 *           jndi-name="ejb/FileSystemMgt"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="FileSystem" ref-name="ejb/FileSystem"
 *              view-type="local"
 * @ejb.ejb-ref ejb-name="Study" ref-name="ejb/Study" view-type="local"
 * @ejb.ejb-ref ejb-name="StudyOnFileSystem" ref-name="ejb/StudyOnFileSystem"
 *              view-type="local"
 */
public abstract class FileSystemMgtBean implements SessionBean {

	private static Logger log = Logger.getLogger(FileSystemMgtBean.class);

	private StudyLocalHome studyHome;

	private StudyOnFileSystemLocalHome sofHome;

	private FileLocalHome fileHome;

	private FileSystemLocalHome fileSystemHome;

	public void setSessionContext(SessionContext ctx) {
		Context jndiCtx = null;
		try {
			jndiCtx = new InitialContext();
			this.studyHome = (StudyLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Study");
			this.sofHome = (StudyOnFileSystemLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/StudyOnFileSystem");
			this.fileHome = (FileLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/File");
			this.fileSystemHome = (FileSystemLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/FileSystem");
		} catch (NamingException e) {
			throw new EJBException(e);
		} finally {
			if (jndiCtx != null) {
				try {
					jndiCtx.close();
				} catch (NamingException ignore) {
				}
			}
		}
	}

	public void unsetSessionContext() {
		studyHome = null;
		sofHome = null;
		fileHome = null;
		fileSystemHome = null;
	}

	/**
	 * @ejb.interface-method
	 */
	public void deleteFile(int file_pk) throws RemoteException, EJBException,
			RemoveException {
		fileHome.remove(new Integer(file_pk));
	}

	/**
	 * @ejb.interface-method
	 */
	public FileDTO[] getDereferencedFiles(String dirPath)
			throws FinderException {
		log.debug("Querying for dereferenced files in " + dirPath);
		Collection c = fileHome.findDereferencedInFileSystem(dirPath);
		FileDTO[] dto = new FileDTO[c.size()];
		Iterator it = c.iterator();
		for (int i = 0; i < dto.length; ++i) {
			dto[i] = toDTO((FileLocal) it.next());
		}
		log.debug("Found " + dto.length + " dereferenced files in " + dirPath);
		return dto;
	}

	/**
	 * @throws CreateException
	 * @ejb.interface-method
	 */
	public FileSystemDTO addFileSystem(FileSystemDTO dto)
			throws CreateException {
		return toDTO(fileSystemHome.create(dto.getDirectoryPath(), dto
				.getRetrieveAET()));
	}

	/**
	 * @throws FinderException
	 * @ejb.interface-method
	 */
	public FileSystemDTO getFileSystem(String dirPath) throws FinderException {
		return toDTO(fileSystemHome.findByDirectoryPath(dirPath));
	}

	/**
	 * @ejb.interface-method
	 */
	public void removeFileSystem(String dirPath) throws FinderException,
			RemoveException {
		fileSystemHome.findByDirectoryPath(dirPath).remove();
	}

	/**
	 * @ejb.interface-method
	 */
	public void removeFileSystem(int pk) throws RemoveException {
		fileSystemHome.remove(new Integer(pk));
	}

	/**
	 * @ejb.interface-method
	 */
	public FileSystemDTO[] getAllFileSystems() throws FinderException {
		Collection c = fileSystemHome.findAll();
		FileSystemDTO[] dto = new FileSystemDTO[c.size()];
		Iterator it = c.iterator();
		for (int i = 0; i < dto.length; i++) {
			dto[i] = toDTO((FileSystemLocal) it.next());
		}
		return dto;
	}

	/**
	 * @ejb.interface-method
	 */
	public void touchStudyOnFileSystem(String siud, String dirPath)
			throws FinderException, CreateException {
		try {
			sofHome.findByStudyAndFileSystem(siud, dirPath).touch();
		} catch (ObjectNotFoundException e) {
			sofHome.create(studyHome.findByStudyIuid(siud), fileSystemHome
					.findByDirectoryPath(dirPath));
		}
	}
	
	private FileSystemDTO toDTO(FileSystemLocal fs) {
		FileSystemDTO dto = new FileSystemDTO();
		dto.setPk(fs.getPk().intValue());
		dto.setDirectoryPath(fs.getDirectoryPath());
		dto.setRetrieveAET(fs.getRetrieveAET());
		return dto;
	}

	private FileDTO toDTO(FileLocal file) {
		FileSystemLocal fs = file.getFileSystem();
		FileDTO dto = new FileDTO();
		dto.setPk(file.getPk().intValue());
		dto.setRetrieveAET(fs.getRetrieveAET());
		dto.setDirectoryPath(fs.getDirectoryPath());
		dto.setFilePath(file.getFilePath());
		dto.setFileTsuid(file.getFileTsuid());
		dto.setFileMd5(file.getFileMd5());
		dto.setFileSize(file.getFileSize());
		return dto;
	}

}