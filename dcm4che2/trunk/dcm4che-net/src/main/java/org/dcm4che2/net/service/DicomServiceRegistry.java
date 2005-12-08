package org.dcm4che2.net.service;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.PDVInputStream;

public interface DicomServiceRegistry
{

    void register(DicomService service);

    CStoreSCP getCStoreSCP(Association as, DicomObject cmd);

    CGetSCP getCGetSCP(Association as, DicomObject cmd);

    CFindSCP getCFindSCP(Association as, DicomObject cmd);

    CMoveSCP getCMoveSCP(Association as, DicomObject cmd);

    CEchoSCP getCEchoSCP(Association as, DicomObject cmd);

    NEventReportSCU getNEventReportSCU(Association as, DicomObject cmd);

    NGetSCP getNGetSCP(Association as, DicomObject cmd);

    NSetSCP getNSetSCP(Association as, DicomObject cmd);

    NActionSCP getNActionSCP(Association as, DicomObject cmd);

    NCreateSCP getNCreateSCP(Association as, DicomObject cmd);

    NDeleteSCP getNDeleteSCP(Association as, DicomObject cmd);

    void process(Association as, int pcid, DicomObject cmd,
            PDVInputStream dataStream, String tsuid);

}
