package com.metoo.nrsm.core.config.mybatisplus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ListStringToJsonTypeHandler implements TypeHandler<List<String>> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            String json = null;
            try {
                json = objectMapper.writeValueAsString(parameter);
            } catch (JsonProcessingException e) {
                throw new SQLException("Error serializing list to JSON", e);
            }
            ps.setString(i, json);
        }
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>(){});
            } catch (IOException e) {
                throw new SQLException("Error deserializing JSON to list", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>(){});
            } catch (IOException e) {
                throw new SQLException("Error deserializing JSON to list", e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<String>>(){});
            } catch (IOException e) {
                throw new SQLException("Error deserializing JSON to list", e);
            }
        }
        return new ArrayList<>();
    }
}