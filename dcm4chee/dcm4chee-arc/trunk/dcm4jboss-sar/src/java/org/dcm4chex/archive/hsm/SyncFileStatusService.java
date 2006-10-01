/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.hsm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.dcm4che.util.Executer;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Nov 22, 2005
 */
public class SyncFileStatusService extends ServiceMBeanSupport {

    private final TimerSupport timer = new TimerSupport(this);

    private static final String NONE = "NONE";

    private static final String INFO_PARAM = "%i";

    private static final String FILE_PARAM = "%f";

    private static final String DIR_PARAM = "%d";

    private static final String TIMER_ID = "SyncFileStatus";

    private long minFileAge = 0L;

    private long taskInterval = 0L;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private int checkFileStatus;

    private int commandFailedFileStatus;

    private int nonZeroExitFileStatus;

    private int matchFileStatus;

    private int noMatchFileStatus;

    private String fileSystem = null;

    private String[] command = { "mmls ", INFO_PARAM, "/", FILE_PARAM };

    private Pattern pattern;

    private Integer listenerID;
    
    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("SyncFileStatus ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                try {
                    check();
                } catch (Exception e) {
                    log.error("check file status failed!", e);
                }
            }
        }

    };

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public final String getFileSystem() {
        return fileSystem == null ? NONE : fileSystem;
    }

    public final void setFileSystem(String fileSystem) {
        this.fileSystem =  (NONE.equalsIgnoreCase(fileSystem)) ? null : fileSystem;
    }

    public final String getCheckFileStatus() {
        return FileStatus.toString(checkFileStatus);
    }

    public final void setCheckFileStatus(String status) {
        this.checkFileStatus = FileStatus.toInt(status);
    }

    public final String getNonZeroExitFileStatus() {
        return FileStatus.toString(nonZeroExitFileStatus);
    }

    public final void setNonZeroExitFileStatus(String status) {
        this.nonZeroExitFileStatus = FileStatus.toInt(status);
    }

    public final String getMatchFileStatus() {
        return FileStatus.toString(matchFileStatus);
    }

    public final void setMatchFileStatus(String status) {
        this.matchFileStatus = FileStatus.toInt(status);
    }

    public final String getNoMatchFileStatus() {
        return FileStatus.toString(noMatchFileStatus);
    }

    public final void setNoMatchFileStatus(String status) {
        this.noMatchFileStatus = FileStatus.toInt(status);
    }

    public final String getCommandFailedFileStatus() {
        return FileStatus.toString(commandFailedFileStatus);
    }

    public final void setCommandFailedFileStatus(String status) {
        this.commandFailedFileStatus = FileStatus.toInt(status);
    }

    public final String getCommand() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < command.length; i++) {
            sb.append(command[i]);
        }
        return sb.toString();
    }

    private String makeCommand(String dirParam, String fileParam,
            String infoParam) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < command.length; i++) {
            sb.append(command[i] == DIR_PARAM ? dirParam
                            : command[i] == FILE_PARAM ? fileParam
                                    : command[i] == INFO_PARAM ? infoParam
                                            : command[i]);
        }
        return sb.toString();
    }

    public final void setCommand(String command) {
        String[] a = StringUtils.split(command, '%');
        try {
            String[] b = new String[a.length + a.length - 1];
            b[0] = a[0];
            for (int i = 1; i < a.length; i++) {
                String s = a[i];
                b[2 * i - 1] = ("%" + s.charAt(0)).intern();
                b[2 * i] = s.substring(1);
            }
            this.command = b;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(command);
        }
    }

    public final String getPattern() {
        return pattern.pattern();
    }

    public final void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern, Pattern.DOTALL);
    }

    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) {
        long oldInterval = taskInterval;
        int pos = interval.indexOf('!');
        if (pos == -1) {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval);
            disabledEndHour = -1;
        } else {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval
                    .substring(0, pos));
            int pos1 = interval.indexOf('-', pos);
            disabledStartHour = Integer.parseInt(interval.substring(pos + 1,
                    pos1));
            disabledEndHour = Integer.parseInt(interval.substring(pos1 + 1));
        }
        if (getState() == STARTED && oldInterval != taskInterval) {
            timer.stopScheduler(TIMER_ID, listenerID, timerListener);
            listenerID = timer.startScheduler(TIMER_ID, taskInterval,
                    timerListener);
        }
    }

    public final String getMinimumFileAge() {
        return RetryIntervalls.formatInterval(minFileAge);
    }

    public final void setMinimumFileAge(String intervall) {
        this.minFileAge = RetryIntervalls.parseInterval(intervall);
    }

    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1)
            return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour;
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        timer.init();
        listenerID = timer.startScheduler(TIMER_ID, taskInterval, 
                timerListener);
    }

    protected void stopService() throws Exception {
        timer.stopScheduler(TIMER_ID, listenerID, timerListener);
        super.stopService();
    }

    public int check() throws Exception {
        if (fileSystem == null) {
            return 0;
        }
        FileSystemMgt fsmgt = newFileSystemMgt();
        FileDTO[] c = fsmgt.findFilesByStatusAndFileSystem(fileSystem,
                checkFileStatus, 
                new Timestamp(System.currentTimeMillis() - minFileAge),
                limitNumberOfFilesPerTask);
        if (c.length == 0) {
            return 0;
        }
        int count = 0;
        HashMap checkedTars = new HashMap();
        for (int i = 0; i < c.length; i++) {
            if (check(fsmgt, c[i], checkedTars))
                ++count;
        }
        return count;
    }

    private boolean check(FileSystemMgt fsmgt, FileDTO fileDTO, HashMap checkedTars) {
        String dirpath = fileDTO.getDirectoryPath();
        String filePath = fileDTO.getFilePath();
        String tarPath = null;
        if (dirpath.startsWith("tar:")) {
            dirpath = dirpath.substring(4);
            filePath = filePath.substring(0, filePath.indexOf('!'));
            tarPath = dirpath + '/' + filePath;
            Integer status = (Integer) checkedTars.get(tarPath);
            if (status != null) {
                return updateFileStatus(fsmgt, fileDTO, status.intValue());                
            }
        }
        int status = queryHSM(dirpath, filePath, fileDTO);
        if (tarPath != null) {
            checkedTars.put(tarPath, new Integer(status));
        }
        return updateFileStatus(fsmgt, fileDTO, status);
    }

    private boolean updateFileStatus(FileSystemMgt fsmgt, FileDTO fileDTO, int status) {
        if (fileDTO.getFileStatus() != status) {
            try {
                fsmgt.setFileStatus(fileDTO.getPk(), status);
                return true;
            } catch (Exception e) {
                log.error("Failed to update status of file " + fileDTO, e);
            }
        }
        return false;
    }

    private int queryHSM(String dirpath, String filePath, FileDTO fileDTO) {
        String cmd = makeCommand(
                dirpath.replace('/', File.separatorChar), 
                filePath.replace('/', File.separatorChar),
                fileDTO.getUserInfo());
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmd, stdout, null);
            int exit = ex.waitFor();
            if (exit != 0) {
                log.info("Non-zero exit code(" + exit + ") of " + cmd);
                return nonZeroExitFileStatus;
            } else {
                String result = stdout.toString();
                return pattern.matcher(result).matches() ? matchFileStatus
                        : noMatchFileStatus;
            }
        } catch (Exception e) {
            log.error("Failed to execute " + cmd, e);
            return commandFailedFileStatus;
        }
    }

    private FileSystemMgt newFileSystemMgt() {
        try {
            FileSystemMgtHome home = (FileSystemMgtHome) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgtHome.class,
                            FileSystemMgtHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }
}
