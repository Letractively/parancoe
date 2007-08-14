// Copyright 2006-2007 The Parancoe Team
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package it.jugpadova.blo;

import it.jugpadova.Daos;
import it.jugpadova.bean.EventSearch;
import it.jugpadova.dao.EventDao;
import it.jugpadova.dao.JuggerDao;
import it.jugpadova.dao.ParticipantDao;
import org.springframework.transaction.annotation.Transactional;
import it.jugpadova.po.Event;
import it.jugpadova.po.Jugger;
import it.jugpadova.po.Participant;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.encoding.MessageDigestPasswordEncoder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.proxy.dwr.Util;
import org.directwebremoting.proxy.scriptaculous.Effect;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class EventBo {

    private static final Logger logger =
            Logger.getLogger(EventBo.class);
    private Daos daos;
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private String confirmationSenderEmailAddress;

    public Daos getDaos() {
        return daos;
    }

    public void setDaos(Daos daos) {
        this.daos = daos;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public String getConfirmationSenderEmailAddress() {
        return confirmationSenderEmailAddress;
    }

    public void setConfirmationSenderEmailAddress(String confirmationSenderEmailAddress) {
        this.confirmationSenderEmailAddress = confirmationSenderEmailAddress;
    }

    @Transactional(readOnly = true)
    public List<Event> retrieveEvents() {
        List<Event> events = getDaos().getEventDao().findCurrentEvents();
        for (Event event : events) {
            event.getParticipants().size();
        }
        return events;
    }

    @Transactional(readOnly = true)
    public List<Event> search(EventSearch eventSearch) {
        List<Event> events = new LinkedList<Event>();
        try {
            DetachedCriteria eventCriteria =
                    DetachedCriteria.forClass(Event.class);
            if (StringUtils.isNotBlank(eventSearch.getJugName()) ||
                    StringUtils.isNotBlank(eventSearch.getCountry()) ||
                    StringUtils.isNotBlank(eventSearch.getContinent())) {
                DetachedCriteria ownerCriteria =
                        eventCriteria.createCriteria("owner");
                if (StringUtils.isNotBlank(eventSearch.getJugName())) {
                    ownerCriteria.add(Restrictions.ilike("jugName",
                            eventSearch.getJugName(),
                            MatchMode.ANYWHERE));
                }
                if (StringUtils.isNotBlank(eventSearch.getCountry()) ||
                        StringUtils.isNotBlank(eventSearch.getContinent())) {
                    DetachedCriteria countryCriteria =
                            ownerCriteria.createCriteria("country");
                    if (StringUtils.isNotBlank(eventSearch.getCountry())) {
                        countryCriteria.add(Restrictions.or(Restrictions.ilike("englishName",
                                eventSearch.getCountry(),
                                MatchMode.ANYWHERE),
                                Restrictions.ilike("localName",
                                eventSearch.getCountry(),
                                MatchMode.ANYWHERE)));
                    }
                    if (StringUtils.isNotBlank(eventSearch.getContinent())) {
                        DetachedCriteria continentCriteria =
                                countryCriteria.createCriteria("continent");
                        continentCriteria.add(Restrictions.ilike("name",
                                eventSearch.getContinent(),
                                MatchMode.ANYWHERE));
                    }
                }
            }
            /**
             * if (params.getStato() != Stato.NON_IMPOSTATO) {
             * revisoreCriteria.add(Restrictions.eq("stato", params.getStato()));
             * }
             * // join con l'anagrafica
             * if (StringUtils.isNotBlank(params.getCognome()) || StringUtils.isNotBlank(params.getNome())
             * || StringUtils.isNotBlank(params.getCodFiscale())) {
             * DetachedCriteria anagraficaCriteria = revisoreCriteria.createCriteria("anagrafica");
             * if (StringUtils.isNotBlank(params.getCodFiscale())) {
             * anagraficaCriteria.add(Restrictions.ilike("codFiscale", params.getCodFiscale(), MatchMode.ANYWHERE));
             * }
             * if (StringUtils.isNotBlank(params.getCognome())) {
             * anagraficaCriteria.add(Restrictions.ilike("cognome", params.getCognome(), MatchMode.ANYWHERE));
             * }
             * if (StringUtils.isNotBlank(params.getNome())) {
             * anagraficaCriteria.add(Restrictions.ilike("nome", params.getNome(), MatchMode.ANYWHERE));
             * }
             * }
             */

            events = daos.getEventDao().searchByCriteria(eventCriteria);
            for (Event event : events) {
                event.getParticipants().size();
            }
        } catch (Exception e) {
            logger.error("Error searching events", e);
        }

        return events;
    }

    @Transactional
    public void save(Event event) {
        if (event.getOwner() == null) {
            Jugger jugger = getCurrentJugger();
            event.setOwner(jugger);
        }
        EventDao eventDao = getDaos().getEventDao();
        eventDao.createOrUpdate(event);
    }

    private Jugger getCurrentJugger() {
        Jugger result = null;
        Authentication authentication =
                org.acegisecurity.context.SecurityContextHolder.getContext().
                getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            JuggerDao juggerDao = getDaos().getJuggerDao();
            List<Jugger> juggers = juggerDao.searchByUsername(name);
            if (juggers.size() > 0) {
                result = juggerDao.searchByUsername(name).get(0);
                if (juggers.size() > 1) {
                    logger.warn("MOre than a JUG with the '" + name +
                            "' username");
                }
            } else {
                logger.error("No jugger with the '" + name + "' username");
            }
        }
        return result;
    }

    @Transactional
    public void register(Event event, Participant participant) {
        EventDao eventDao = getDaos().getEventDao();
        event = eventDao.read(event.getId());
        participant.setConfirmed(Boolean.FALSE);
        participant.setConfirmationCode(generateConfirmationCode(event,
                participant));
        participant.setEvent(event);
        getDaos().getParticipantDao().createOrUpdate(participant);
        event.addParticipant(participant);
        eventDao.createOrUpdate(event);
        sendConfirmationEmail(event, participant);
    }

    @Transactional
    public void refreshRegistration(Event event,
            Participant participant) {
        participant.setConfirmed(Boolean.FALSE);
        participant.setConfirmationCode(generateConfirmationCode(event,
                participant));
        participant.setEvent(event);
        getDaos().getParticipantDao().createOrUpdate(participant);
        sendConfirmationEmail(event, participant);
    }

    private String generateConfirmationCode(Event event,
            Participant participant) {
        return new MessageDigestPasswordEncoder("MD5", true).encodePassword(event.getTitle() +
                participant.getFirstName() + participant.getLastName() +
                participant.getEmail(), new Date());
    }

    private void sendConfirmationEmail(final Event event,
            final Participant participant) {
        MimeMessagePreparator preparator =
                new MimeMessagePreparator() {

            @SuppressWarnings(value = "unchecked")
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message =
                        new MimeMessageHelper(mimeMessage);
                message.setTo(participant.getEmail());
                message.setFrom(confirmationSenderEmailAddress);
                message.setSubject("Please confirm event registration");
                Map model = new HashMap();
                model.put("participant", participant);
                model.put("event", event);
                model.put("confirmationCode",
                        URLEncoder.encode(participant.getConfirmationCode(),
                        "UTF-8"));
                model.put("email",
                        URLEncoder.encode(participant.getEmail(), "UTF-8"));
                String text =
                        VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                        "it/jugpadova/registration-confirmation.vm", model);
                message.setText(text, true);
            }
        };
        this.mailSender.send(preparator);
    }

    @Transactional(readOnly = true)
    public Event retrieveEvent(Long id) {
        Event event = getDaos().getEventDao().read(id);
        event.getParticipants().size();
        return event;
    }

    @Transactional(readOnly = true)
    public List findPartialLocation(String partialLocation,
            String username) {
        List<String> result = new ArrayList<String>();
        if (!StringUtils.isBlank(partialLocation) &&
                !StringUtils.isBlank(username)) {
            try {
                List<Event> events =
                        getDaos().getEventDao().
                        findEventByPartialLocationAndOwner("%" + partialLocation +
                        "%", username);
                Iterator<Event> itEvents = events.iterator();
                while (itEvents.hasNext()) {
                    Event event = itEvents.next();
                    result.add(event.getLocation() + "<div class=\"informal\">" +
                            event.getDirections() + "</div>" +
                            "<div class=\"informal hidden\">" + event.getId() +
                            "</div>");
                }
            } catch (Exception e) {
                logger.error("Error completing the location", e);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public void copyDirectionsFromEvent(long eventId) {
        WebContext wctx = WebContextFactory.get();
        ScriptSession session = wctx.getScriptSession();
        Util util = new Util(session);
        Event event = daos.getEventDao().read(Long.valueOf(eventId));
        if (event != null) {
            util.setValue("location", event.getLocation());
            util.setValue("directions", event.getDirections());
            util.setValue("directionsPreview",
                    FilterBo.filterText(event.getDirections(), event.getFilter(),
                    false));
            Effect effect =
                    new Effect(session);
            effect.highlight("location");
            effect.highlight("directions");
            effect.highlight("directionsPreview");
        }
    }

    @Transactional
    public Participant confirmParticipant(String email,
            String confirmationCode) {
        ParticipantDao dao = daos.getParticipantDao();
        List<Participant> participants =
                dao.findByEmailAndConfirmationCodeAndConfirmed(email,
                confirmationCode, Boolean.FALSE);
        if (participants != null && participants.size() > 0) {
            Participant p = participants.get(0);
            p.setConfirmed(Boolean.TRUE);
            return p;
        }
        return null;
    }
}
