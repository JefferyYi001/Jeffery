// ORM class for table 'ads_user_action_convert_day'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Wed Apr 15 00:37:55 CST 2020
// For connector: org.apache.sqoop.manager.MySQLManager
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.lib.db.DBWritable;
import com.cloudera.sqoop.lib.JdbcWritableBridge;
import com.cloudera.sqoop.lib.DelimiterSet;
import com.cloudera.sqoop.lib.FieldFormatter;
import com.cloudera.sqoop.lib.RecordParser;
import com.cloudera.sqoop.lib.BooleanParser;
import com.cloudera.sqoop.lib.BlobRef;
import com.cloudera.sqoop.lib.ClobRef;
import com.cloudera.sqoop.lib.LargeObjectLoader;
import com.cloudera.sqoop.lib.SqoopRecord;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ads_user_action_convert_day extends SqoopRecord  implements DBWritable, Writable {
  private final int PROTOCOL_VERSION = 3;
  public int getClassFormatVersion() { return PROTOCOL_VERSION; }
  protected ResultSet __cur_result_set;
  private String dt;
  public String get_dt() {
    return dt;
  }
  public void set_dt(String dt) {
    this.dt = dt;
  }
  public ads_user_action_convert_day with_dt(String dt) {
    this.dt = dt;
    return this;
  }
  private Long total_visitor_m_count;
  public Long get_total_visitor_m_count() {
    return total_visitor_m_count;
  }
  public void set_total_visitor_m_count(Long total_visitor_m_count) {
    this.total_visitor_m_count = total_visitor_m_count;
  }
  public ads_user_action_convert_day with_total_visitor_m_count(Long total_visitor_m_count) {
    this.total_visitor_m_count = total_visitor_m_count;
    return this;
  }
  private Long order_u_count;
  public Long get_order_u_count() {
    return order_u_count;
  }
  public void set_order_u_count(Long order_u_count) {
    this.order_u_count = order_u_count;
  }
  public ads_user_action_convert_day with_order_u_count(Long order_u_count) {
    this.order_u_count = order_u_count;
    return this;
  }
  private java.math.BigDecimal visitor2order_convert_ratio;
  public java.math.BigDecimal get_visitor2order_convert_ratio() {
    return visitor2order_convert_ratio;
  }
  public void set_visitor2order_convert_ratio(java.math.BigDecimal visitor2order_convert_ratio) {
    this.visitor2order_convert_ratio = visitor2order_convert_ratio;
  }
  public ads_user_action_convert_day with_visitor2order_convert_ratio(java.math.BigDecimal visitor2order_convert_ratio) {
    this.visitor2order_convert_ratio = visitor2order_convert_ratio;
    return this;
  }
  private Long payment_u_count;
  public Long get_payment_u_count() {
    return payment_u_count;
  }
  public void set_payment_u_count(Long payment_u_count) {
    this.payment_u_count = payment_u_count;
  }
  public ads_user_action_convert_day with_payment_u_count(Long payment_u_count) {
    this.payment_u_count = payment_u_count;
    return this;
  }
  private java.math.BigDecimal order2payment_convert_ratio;
  public java.math.BigDecimal get_order2payment_convert_ratio() {
    return order2payment_convert_ratio;
  }
  public void set_order2payment_convert_ratio(java.math.BigDecimal order2payment_convert_ratio) {
    this.order2payment_convert_ratio = order2payment_convert_ratio;
  }
  public ads_user_action_convert_day with_order2payment_convert_ratio(java.math.BigDecimal order2payment_convert_ratio) {
    this.order2payment_convert_ratio = order2payment_convert_ratio;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_user_action_convert_day)) {
      return false;
    }
    ads_user_action_convert_day that = (ads_user_action_convert_day) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.total_visitor_m_count == null ? that.total_visitor_m_count == null : this.total_visitor_m_count.equals(that.total_visitor_m_count));
    equal = equal && (this.order_u_count == null ? that.order_u_count == null : this.order_u_count.equals(that.order_u_count));
    equal = equal && (this.visitor2order_convert_ratio == null ? that.visitor2order_convert_ratio == null : this.visitor2order_convert_ratio.equals(that.visitor2order_convert_ratio));
    equal = equal && (this.payment_u_count == null ? that.payment_u_count == null : this.payment_u_count.equals(that.payment_u_count));
    equal = equal && (this.order2payment_convert_ratio == null ? that.order2payment_convert_ratio == null : this.order2payment_convert_ratio.equals(that.order2payment_convert_ratio));
    return equal;
  }
  public boolean equals0(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_user_action_convert_day)) {
      return false;
    }
    ads_user_action_convert_day that = (ads_user_action_convert_day) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.total_visitor_m_count == null ? that.total_visitor_m_count == null : this.total_visitor_m_count.equals(that.total_visitor_m_count));
    equal = equal && (this.order_u_count == null ? that.order_u_count == null : this.order_u_count.equals(that.order_u_count));
    equal = equal && (this.visitor2order_convert_ratio == null ? that.visitor2order_convert_ratio == null : this.visitor2order_convert_ratio.equals(that.visitor2order_convert_ratio));
    equal = equal && (this.payment_u_count == null ? that.payment_u_count == null : this.payment_u_count.equals(that.payment_u_count));
    equal = equal && (this.order2payment_convert_ratio == null ? that.order2payment_convert_ratio == null : this.order2payment_convert_ratio.equals(that.order2payment_convert_ratio));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.total_visitor_m_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.order_u_count = JdbcWritableBridge.readLong(3, __dbResults);
    this.visitor2order_convert_ratio = JdbcWritableBridge.readBigDecimal(4, __dbResults);
    this.payment_u_count = JdbcWritableBridge.readLong(5, __dbResults);
    this.order2payment_convert_ratio = JdbcWritableBridge.readBigDecimal(6, __dbResults);
  }
  public void readFields0(ResultSet __dbResults) throws SQLException {
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.total_visitor_m_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.order_u_count = JdbcWritableBridge.readLong(3, __dbResults);
    this.visitor2order_convert_ratio = JdbcWritableBridge.readBigDecimal(4, __dbResults);
    this.payment_u_count = JdbcWritableBridge.readLong(5, __dbResults);
    this.order2payment_convert_ratio = JdbcWritableBridge.readBigDecimal(6, __dbResults);
  }
  public void loadLargeObjects(LargeObjectLoader __loader)
      throws SQLException, IOException, InterruptedException {
  }
  public void loadLargeObjects0(LargeObjectLoader __loader)
      throws SQLException, IOException, InterruptedException {
  }
  public void write(PreparedStatement __dbStmt) throws SQLException {
    write(__dbStmt, 0);
  }

  public int write(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(dt, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(total_visitor_m_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(order_u_count, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(visitor2order_convert_ratio, 4 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeLong(payment_u_count, 5 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(order2payment_convert_ratio, 6 + __off, 3, __dbStmt);
    return 6;
  }
  public void write0(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(dt, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(total_visitor_m_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(order_u_count, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(visitor2order_convert_ratio, 4 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeLong(payment_u_count, 5 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(order2payment_convert_ratio, 6 + __off, 3, __dbStmt);
  }
  public void readFields(DataInput __dataIn) throws IOException {
this.readFields0(__dataIn);  }
  public void readFields0(DataInput __dataIn) throws IOException {
    if (__dataIn.readBoolean()) { 
        this.dt = null;
    } else {
    this.dt = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.total_visitor_m_count = null;
    } else {
    this.total_visitor_m_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.order_u_count = null;
    } else {
    this.order_u_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.visitor2order_convert_ratio = null;
    } else {
    this.visitor2order_convert_ratio = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.payment_u_count = null;
    } else {
    this.payment_u_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.order2payment_convert_ratio = null;
    } else {
    this.order2payment_convert_ratio = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.total_visitor_m_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.total_visitor_m_count);
    }
    if (null == this.order_u_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.order_u_count);
    }
    if (null == this.visitor2order_convert_ratio) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.visitor2order_convert_ratio, __dataOut);
    }
    if (null == this.payment_u_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.payment_u_count);
    }
    if (null == this.order2payment_convert_ratio) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.order2payment_convert_ratio, __dataOut);
    }
  }
  public void write0(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.total_visitor_m_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.total_visitor_m_count);
    }
    if (null == this.order_u_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.order_u_count);
    }
    if (null == this.visitor2order_convert_ratio) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.visitor2order_convert_ratio, __dataOut);
    }
    if (null == this.payment_u_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.payment_u_count);
    }
    if (null == this.order2payment_convert_ratio) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.order2payment_convert_ratio, __dataOut);
    }
  }
  private static final DelimiterSet __outputDelimiters = new DelimiterSet((char) 44, (char) 10, (char) 0, (char) 0, false);
  public String toString() {
    return toString(__outputDelimiters, true);
  }
  public String toString(DelimiterSet delimiters) {
    return toString(delimiters, true);
  }
  public String toString(boolean useRecordDelim) {
    return toString(__outputDelimiters, useRecordDelim);
  }
  public String toString(DelimiterSet delimiters, boolean useRecordDelim) {
    StringBuilder __sb = new StringBuilder();
    char fieldDelim = delimiters.getFieldsTerminatedBy();
    __sb.append(FieldFormatter.escapeAndEnclose(dt==null?"null":dt, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(total_visitor_m_count==null?"null":"" + total_visitor_m_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order_u_count==null?"null":"" + order_u_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(visitor2order_convert_ratio==null?"null":visitor2order_convert_ratio.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_u_count==null?"null":"" + payment_u_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order2payment_convert_ratio==null?"null":order2payment_convert_ratio.toPlainString(), delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  public void toString0(DelimiterSet delimiters, StringBuilder __sb, char fieldDelim) {
    __sb.append(FieldFormatter.escapeAndEnclose(dt==null?"null":dt, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(total_visitor_m_count==null?"null":"" + total_visitor_m_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order_u_count==null?"null":"" + order_u_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(visitor2order_convert_ratio==null?"null":visitor2order_convert_ratio.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_u_count==null?"null":"" + payment_u_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order2payment_convert_ratio==null?"null":order2payment_convert_ratio.toPlainString(), delimiters));
  }
  private static final DelimiterSet __inputDelimiters = new DelimiterSet((char) 9, (char) 10, (char) 0, (char) 0, false);
  private RecordParser __parser;
  public void parse(Text __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(CharSequence __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(byte [] __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(char [] __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(ByteBuffer __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(CharBuffer __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  private void __loadFromFields(List<String> fields) {
    Iterator<String> __it = fields.listIterator();
    String __cur_str = null;
    try {
    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.dt = null; } else {
      this.dt = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.total_visitor_m_count = null; } else {
      this.total_visitor_m_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.order_u_count = null; } else {
      this.order_u_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.visitor2order_convert_ratio = null; } else {
      this.visitor2order_convert_ratio = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_u_count = null; } else {
      this.payment_u_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.order2payment_convert_ratio = null; } else {
      this.order2payment_convert_ratio = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  private void __loadFromFields0(Iterator<String> __it) {
    String __cur_str = null;
    try {
    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.dt = null; } else {
      this.dt = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.total_visitor_m_count = null; } else {
      this.total_visitor_m_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.order_u_count = null; } else {
      this.order_u_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.visitor2order_convert_ratio = null; } else {
      this.visitor2order_convert_ratio = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_u_count = null; } else {
      this.payment_u_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.order2payment_convert_ratio = null; } else {
      this.order2payment_convert_ratio = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  public Object clone() throws CloneNotSupportedException {
    ads_user_action_convert_day o = (ads_user_action_convert_day) super.clone();
    return o;
  }

  public void clone0(ads_user_action_convert_day o) throws CloneNotSupportedException {
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("total_visitor_m_count", this.total_visitor_m_count);
    __sqoop$field_map.put("order_u_count", this.order_u_count);
    __sqoop$field_map.put("visitor2order_convert_ratio", this.visitor2order_convert_ratio);
    __sqoop$field_map.put("payment_u_count", this.payment_u_count);
    __sqoop$field_map.put("order2payment_convert_ratio", this.order2payment_convert_ratio);
    return __sqoop$field_map;
  }

  public void getFieldMap0(Map<String, Object> __sqoop$field_map) {
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("total_visitor_m_count", this.total_visitor_m_count);
    __sqoop$field_map.put("order_u_count", this.order_u_count);
    __sqoop$field_map.put("visitor2order_convert_ratio", this.visitor2order_convert_ratio);
    __sqoop$field_map.put("payment_u_count", this.payment_u_count);
    __sqoop$field_map.put("order2payment_convert_ratio", this.order2payment_convert_ratio);
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("dt".equals(__fieldName)) {
      this.dt = (String) __fieldVal;
    }
    else    if ("total_visitor_m_count".equals(__fieldName)) {
      this.total_visitor_m_count = (Long) __fieldVal;
    }
    else    if ("order_u_count".equals(__fieldName)) {
      this.order_u_count = (Long) __fieldVal;
    }
    else    if ("visitor2order_convert_ratio".equals(__fieldName)) {
      this.visitor2order_convert_ratio = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("payment_u_count".equals(__fieldName)) {
      this.payment_u_count = (Long) __fieldVal;
    }
    else    if ("order2payment_convert_ratio".equals(__fieldName)) {
      this.order2payment_convert_ratio = (java.math.BigDecimal) __fieldVal;
    }
    else {
      throw new RuntimeException("No such field: " + __fieldName);
    }
  }
  public boolean setField0(String __fieldName, Object __fieldVal) {
    if ("dt".equals(__fieldName)) {
      this.dt = (String) __fieldVal;
      return true;
    }
    else    if ("total_visitor_m_count".equals(__fieldName)) {
      this.total_visitor_m_count = (Long) __fieldVal;
      return true;
    }
    else    if ("order_u_count".equals(__fieldName)) {
      this.order_u_count = (Long) __fieldVal;
      return true;
    }
    else    if ("visitor2order_convert_ratio".equals(__fieldName)) {
      this.visitor2order_convert_ratio = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("payment_u_count".equals(__fieldName)) {
      this.payment_u_count = (Long) __fieldVal;
      return true;
    }
    else    if ("order2payment_convert_ratio".equals(__fieldName)) {
      this.order2payment_convert_ratio = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else {
      return false;    }
  }
}
