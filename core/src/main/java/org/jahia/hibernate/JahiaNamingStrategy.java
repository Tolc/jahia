package org.jahia.hibernate;

import org.apache.commons.io.IOUtils;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom naming strategy to prefix the table names, prefix any name that uses an SQL reserved word, and can force names to use lowercase.
 */
public class JahiaNamingStrategy extends ImprovedNamingStrategy implements Serializable {

    private static final long serialVersionUID = 2436201913019906777L;

    /**
     * A convenient singleton instance
     */
    public static final NamingStrategy INSTANCE = new JahiaNamingStrategy();

    private static String[] sqlReservedWords = new String[0];

    static {
        InputStream sqlReservedWordsStream = JahiaNamingStrategy.class.getClassLoader().getResourceAsStream(
                "org/jahia/hibernate/sqlReservedWords.txt");
        if (sqlReservedWordsStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sqlReservedWordsStream));
            String newLine = null;
            List<String> reservedWordList = new ArrayList<String>();
            try {
                while ((newLine = bufferedReader.readLine()) != null) {
                    reservedWordList.add(newLine.trim().toLowerCase());
                }
                sqlReservedWords = reservedWordList.toArray(new String[reservedWordList.size()]);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(bufferedReader);
            }
        }
    }

    @Override
    public String classToTableName(String className) {
        String tableName = super.classToTableName(className);
        return processTableName(tableName);
    }

    @Override
    public String propertyToColumnName(String propertyName) {
        String columnName = super.propertyToColumnName(propertyName);
        return processColumnName(columnName);
    }

    @Override
    public String tableName(String tableName) {
        return processTableName(super.tableName(tableName));
    }

    @Override
    public String columnName(String columnName) {
        return processColumnName(super.columnName(columnName));
    }

    public static boolean isSqlReservedWord(String name) {
        String lowerCaseName = name.toLowerCase();
        for (String reservedWord : sqlReservedWords) {
            if (reservedWord.equals(lowerCaseName)) {
                return true;
            }
        }
        return false;
    }

    public static String processTableName(String tableName) {
        return processNameCase("jbpm_" + tableName);
    }

    public static String processColumnName(String columnName) {
        return processNameCase(prefixSqlReservedWords(columnName));
    }

    public static String prefixSqlReservedWords(String name) {
        if (isSqlReservedWord(name)) {
            return "r_" + name;
        } else {
            return name;
        }
    }

    public static String processNameCase(String name) {
        return name.toLowerCase();
    }
}
