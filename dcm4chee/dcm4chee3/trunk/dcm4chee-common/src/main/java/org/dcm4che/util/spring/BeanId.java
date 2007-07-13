package org.dcm4che.util.spring;

/**
 * Identify Spring beans.
 * <p>
 * Some Spring beans are loaded with static references (the DICOM services,
 * etc.). This enum class is for those places where we use direct references to
 * get handles to the beans.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public enum BeanId {
    STORAGE("storageManager"), FS_MGMT("fileSystemManager"), MPPS_MGR(
            "mppsManager"), AE_MGR("aeManager"), STUDY_MGT("studyManager"), GPWL_MGR(
            "gpwlManager"), GPPPS_MGR("gpppsManager"), CONTENT_MGR(
            "contentManager"), MWL_MGR("mwlManager"), MPPS_EMU(
            "mppsEmulationManager"), CODE_DAO("codeDAO"), OPID_DAO("opidDAO"), STUDY_DAO(
            "studyDAO"), SOF_DAO("sofDAO"), HP_STORAGE("hpStorage"), PIX_QUERY(
            "pixQuery"), PRIV_MGR("privateManager"), CONTENT_EDIT("contentEdit"), CHECK_STUDY_PAT(
            "checkStudyPatient"), CODE2DEV_MAPPING("code2DeviceMapping"), CONS_CHECK(
            "consistencyCheck"), STUDY_RECONCILE("studyReconciliation"), FIX_PAT_ATTR(
            "fixPatientAttrs");

    private final String id;

    private BeanId(String id) {
        this.id = id;
    }

    /**
     * Get the Spring bean id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

}
