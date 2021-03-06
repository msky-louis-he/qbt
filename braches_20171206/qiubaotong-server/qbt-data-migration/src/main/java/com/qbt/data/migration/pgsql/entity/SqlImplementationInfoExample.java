package com.qbt.data.migration.pgsql.entity;

import java.util.ArrayList;
import java.util.List;

public class SqlImplementationInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SqlImplementationInfoExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andImplementation_info_idIsNull() {
            addCriterion("implementation_info_id is null");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idIsNotNull() {
            addCriterion("implementation_info_id is not null");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idEqualTo(Object value) {
            addCriterion("implementation_info_id =", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idNotEqualTo(Object value) {
            addCriterion("implementation_info_id <>", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idGreaterThan(Object value) {
            addCriterion("implementation_info_id >", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idGreaterThanOrEqualTo(Object value) {
            addCriterion("implementation_info_id >=", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idLessThan(Object value) {
            addCriterion("implementation_info_id <", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idLessThanOrEqualTo(Object value) {
            addCriterion("implementation_info_id <=", value, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idIn(List<Object> values) {
            addCriterion("implementation_info_id in", values, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idNotIn(List<Object> values) {
            addCriterion("implementation_info_id not in", values, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idBetween(Object value1, Object value2) {
            addCriterion("implementation_info_id between", value1, value2, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_idNotBetween(Object value1, Object value2) {
            addCriterion("implementation_info_id not between", value1, value2, "implementation_info_id");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameIsNull() {
            addCriterion("implementation_info_name is null");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameIsNotNull() {
            addCriterion("implementation_info_name is not null");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameEqualTo(Object value) {
            addCriterion("implementation_info_name =", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameNotEqualTo(Object value) {
            addCriterion("implementation_info_name <>", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameGreaterThan(Object value) {
            addCriterion("implementation_info_name >", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameGreaterThanOrEqualTo(Object value) {
            addCriterion("implementation_info_name >=", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameLessThan(Object value) {
            addCriterion("implementation_info_name <", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameLessThanOrEqualTo(Object value) {
            addCriterion("implementation_info_name <=", value, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameIn(List<Object> values) {
            addCriterion("implementation_info_name in", values, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameNotIn(List<Object> values) {
            addCriterion("implementation_info_name not in", values, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameBetween(Object value1, Object value2) {
            addCriterion("implementation_info_name between", value1, value2, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andImplementation_info_nameNotBetween(Object value1, Object value2) {
            addCriterion("implementation_info_name not between", value1, value2, "implementation_info_name");
            return (Criteria) this;
        }

        public Criteria andInteger_valueIsNull() {
            addCriterion("integer_value is null");
            return (Criteria) this;
        }

        public Criteria andInteger_valueIsNotNull() {
            addCriterion("integer_value is not null");
            return (Criteria) this;
        }

        public Criteria andInteger_valueEqualTo(Object value) {
            addCriterion("integer_value =", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueNotEqualTo(Object value) {
            addCriterion("integer_value <>", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueGreaterThan(Object value) {
            addCriterion("integer_value >", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueGreaterThanOrEqualTo(Object value) {
            addCriterion("integer_value >=", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueLessThan(Object value) {
            addCriterion("integer_value <", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueLessThanOrEqualTo(Object value) {
            addCriterion("integer_value <=", value, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueIn(List<Object> values) {
            addCriterion("integer_value in", values, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueNotIn(List<Object> values) {
            addCriterion("integer_value not in", values, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueBetween(Object value1, Object value2) {
            addCriterion("integer_value between", value1, value2, "integer_value");
            return (Criteria) this;
        }

        public Criteria andInteger_valueNotBetween(Object value1, Object value2) {
            addCriterion("integer_value not between", value1, value2, "integer_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueIsNull() {
            addCriterion("character_value is null");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueIsNotNull() {
            addCriterion("character_value is not null");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueEqualTo(Object value) {
            addCriterion("character_value =", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueNotEqualTo(Object value) {
            addCriterion("character_value <>", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueGreaterThan(Object value) {
            addCriterion("character_value >", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueGreaterThanOrEqualTo(Object value) {
            addCriterion("character_value >=", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueLessThan(Object value) {
            addCriterion("character_value <", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueLessThanOrEqualTo(Object value) {
            addCriterion("character_value <=", value, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueIn(List<Object> values) {
            addCriterion("character_value in", values, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueNotIn(List<Object> values) {
            addCriterion("character_value not in", values, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueBetween(Object value1, Object value2) {
            addCriterion("character_value between", value1, value2, "character_value");
            return (Criteria) this;
        }

        public Criteria andCharacter_valueNotBetween(Object value1, Object value2) {
            addCriterion("character_value not between", value1, value2, "character_value");
            return (Criteria) this;
        }

        public Criteria andCommentsIsNull() {
            addCriterion("comments is null");
            return (Criteria) this;
        }

        public Criteria andCommentsIsNotNull() {
            addCriterion("comments is not null");
            return (Criteria) this;
        }

        public Criteria andCommentsEqualTo(Object value) {
            addCriterion("comments =", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotEqualTo(Object value) {
            addCriterion("comments <>", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsGreaterThan(Object value) {
            addCriterion("comments >", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsGreaterThanOrEqualTo(Object value) {
            addCriterion("comments >=", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsLessThan(Object value) {
            addCriterion("comments <", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsLessThanOrEqualTo(Object value) {
            addCriterion("comments <=", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsIn(List<Object> values) {
            addCriterion("comments in", values, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotIn(List<Object> values) {
            addCriterion("comments not in", values, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsBetween(Object value1, Object value2) {
            addCriterion("comments between", value1, value2, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotBetween(Object value1, Object value2) {
            addCriterion("comments not between", value1, value2, "comments");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}