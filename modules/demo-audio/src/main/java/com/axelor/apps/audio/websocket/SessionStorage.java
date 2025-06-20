package com.axelor.apps.audio.websocket;

import com.axelor.apps.audio.db.CustomsOffice;
import com.axelor.apps.audio.db.JobLog;
import com.axelor.apps.audio.db.repo.CustomsOfficeRepo;
import com.axelor.apps.audio.db.repo.JobLogRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SessionStorage {
    private final CustomsOfficeRepo customsOfficeRepo;
    private final JobLogRepository jobLogRepository;
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    public SessionStorage(CustomsOfficeRepo customsOfficeRepo, JobLogRepository jobLogRepository) {
        this.customsOfficeRepo = customsOfficeRepo;
        this.jobLogRepository = jobLogRepository;
    }

    public void addSession(Session session, Long id) {
        sessions.put(id, session);
    }

    public void removeSession(String sessionId) {
        sessions.values().removeIf(s -> s.getId().equals(sessionId));
    }

    public Session getSession(Long id) {
        return sessions.get(id);
    }

    @Transactional(rollbackOn = {Exception.class})
    public void sendTo(Long id, String message) {
        Session session = getSession(id);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                this.saveLogs(id, JobLogRepository.STATUS_SUCCESS);
                LOG.debug("Message sent: {}", message);
            } catch (IOException e) {
                LOG.error("Message not sent", e);
            }
        } else {
            this.saveLogs(id, JobLogRepository.STATUS_FAILED);
        }
    }

    private void saveLogs(Long id, String status) {
        CustomsOffice customsOffice = customsOfficeRepo.find(id);
        JobLog jobLog = new JobLog();
        jobLog.setCustomOffice(customsOffice);
        jobLog.setSentAt(LocalDateTime.now());
        jobLog.setStatus(status);
        jobLogRepository.save(jobLog);
    }
}
