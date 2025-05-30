package com.axelor.apps.audio.service.impl;

import com.axelor.apps.audio.service.SearchService;
import com.axelor.db.JPA;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.rpc.filter.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.axelor.common.StringUtils.isBlank;

public class SearchServiceImpl implements SearchService {

    private final Provider<JpaSecurity> security;
    private final ObjectMapper objectMapper;

    @Inject
    public SearchServiceImpl(Provider<JpaSecurity> security, ObjectMapper objectMapper) {
        this.security = security;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Map<String, Object>> findAndSort(String model, String where, String orderBy, String name) throws ClassNotFoundException {
        if (isBlank(model)) {
            return null;
        }

        Class<? extends Model> type = (Class<? extends Model>) Class.forName(model);
        Filter securityFilter = getSecurityFilter(type);
        String finalFilter = null;
        if (securityFilter != null) {
            String contion = securityFilter.toString();
            String params = securityFilter.getParams().toString();
            String filter2 = contion.replace("(?)", String.valueOf(params));
            finalFilter = filter2.replace("[", "'").replace("]", "'");
        }

        StringBuilder queryString = new StringBuilder("SELECT self FROM ")
                .append(model)
                .append(" self ");

        buildWhereClause(queryString, where, finalFilter, name);
        buildOrderByClause(queryString, orderBy, name);

        Query query = JPA.em().createQuery(queryString.toString());

        if (!isBlank(name)) {
            query.setParameter("name", "%" + name + "%");
            query.setParameter("nameParam", "%" + name + "%");
        }

        List<Object> objects = query.getResultList();
        return convertToMap(objects);
    }

    private Filter getSecurityFilter(Class<? extends Model> type) {
        return security.get().getFilter(JpaSecurity.CAN_READ, type);
    }

    private void buildWhereClause(StringBuilder queryString, String where, String finalFilter, String name) {
        if (!isBlank(name)) {
            queryString.append("LEFT JOIN MetaTranslation t ON (t.key = CONCAT('value:', self.name) ")
                    .append("AND t.language = 'en') ")
                    .append("WHERE (t.message LIKE UPPER(:name) OR self.name LIKE UPPER(:nameParam))");

            if (!isBlank(where)) {
                queryString.append(" AND ").append(where);
            }

            if (finalFilter != null) {
                queryString.append(" AND ").append(finalFilter);
            }
        } else if (!isBlank(where)) {
            queryString.append("WHERE ").append(where);

            if (finalFilter != null) {
                queryString.append(" AND ").append(finalFilter);
            }
        } else if (finalFilter != null) {
            queryString.append("WHERE ").append(finalFilter);
        }
    }

    private void buildOrderByClause(StringBuilder queryString, String orderBy, String name) {
        if (!isBlank(orderBy) && isBlank(name)) {
            queryString.append(" ORDER BY ").append(orderBy);
        }
    }

    public List<Map<String, Object>> convertToMap(List<Object> objects) {
        List<Map<String, Object>> objectList = new ArrayList<>();
        for (Object object : objects) {
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);
            objectList.add(map);
        }
        return objectList;
    }
}
