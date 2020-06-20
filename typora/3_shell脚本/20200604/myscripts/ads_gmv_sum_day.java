// ORM class for table 'ads_gmv_sum_day'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Wed Apr 15 00:38:09 CST 2020
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

public class ads_gmv_sum_day extends SqoopRecord  implements DBWritable, Writable {
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
  public ads_gmv_sum_day with_dt(String dt) {
    this.dt = dt;
    return this;
  }
  private Long gmv_count;
  public Long get_gmv_count() {
    return gmv_count;
  }
  public void set_gmv_count(Long gmv_count) {
    this.gmv_count = gmv_count;
  }
  public ads_gmv_sum_day with_gmv_count(Long gmv_count) {
    this.gmv_count = gmv_count;
    return this;
  }
  private java.math.BigDecimal gmv_amount;
  public java.math.BigDecimal get_gmv_amount() {
    return gmv_amount;
  }
  public void set_gmv_amount(java.math.BigDecimal gmv_amount) {
    this.gmv_amount = gmv_amount;
  }
  public ads_gmv_sum_day with_gmv_amount(java.math.BigDecimal gmv_amount) {
    this.gmv_amount = gmv_amount;
    return this;
  }
  private java.math.BigDecimal gmv_payment;
  public java.math.BigDecimal get_gmv_payment() {
    return gmv_payment;
  }
  public void set_gmv_payment(java.math.BigDecimal gmv_payment) {
    this.gmv_payment = gmv_payment;
  }
  public ads_gmv_sum_day with_gmv_payment(java.math.BigDecimal gmv_payment) {
    this.gmv_payment = gmv_payment;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_gmv_sum_day)) {
      return false;
    }
    ads_gmv_sum_day that = (ads_gmv_sum_day) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.gmv_count == null ? that.gmv_count == null : this.gmv_count.equals(that.gmv_count));
    equal = equal && (this.gmv_amount == null ? that.gmv_amount == null : this.gmv_amount.equals(that.gmv_amount));
    equal = equal && (this.gmv_payment == null ? that.gmv_payment == null : this.gmv_payment.equals(that.gmv_payment));
    return equal;
  }
  public boolean equals0(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_gmv_sum_day)) {
      return false;
    }
    ads_gmv_sum_day that = (ads_gmv_sum_day) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.gmv_count == null ? that.gmv_count == null : this.gmv_count.equals(that.gmv_count));
    equal = equal && (this.gmv_amount == null ? that.gmv_amount == null : this.gmv_amount.equals(that.gmv_amount));
    equal = equal && (this.gmv_payment == null ? that.gmv_payment == null : this.gmv_payment.equals(that.gmv_payment));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.gmv_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.gmv_amount = JdbcWritableBridge.readBigDecimal(3, __dbResults);
    this.gmv_payment = JdbcWritableBridge.readBigDecimal(4, __dbResults);
  }
  public void readFields0(ResultSet __dbResults) throws SQLException {
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.gmv_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.gmv_amount = JdbcWritableBridge.readBigDecimal(3, __dbResults);
    this.gmv_payment = JdbcWritableBridge.readBigDecimal(4, __dbResults);
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
    JdbcWritableBridge.writeLong(gmv_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(gmv_amount, 3 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(gmv_payment, 4 + __off, 3, __dbStmt);
    return 4;
  }
  public void write0(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(dt, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(gmv_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(gmv_amount, 3 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(gmv_payment, 4 + __off, 3, __dbStmt);
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
        this.gmv_count = null;
    } else {
    this.gmv_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.gmv_amount = null;
    } else {
    this.gmv_amount = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.gmv_payment = null;
    } else {
    this.gmv_payment = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.gmv_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.gmv_count);
    }
    if (null == this.gmv_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.gmv_amount, __dataOut);
    }
    if (null == this.gmv_payment) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.gmv_payment, __dataOut);
    }
  }
  public void write0(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.gmv_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.gmv_count);
    }
    if (null == this.gmv_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.gmv_amount, __dataOut);
    }
    if (null == this.gmv_payment) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.gmv_payment, __dataOut);
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
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_count==null?"null":"" + gmv_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_amount==null?"null":gmv_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_payment==null?"null":gmv_payment.toPlainString(), delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  public void toString0(DelimiterSet delimiters, StringBuilder __sb, char fieldDelim) {
    __sb.append(FieldFormatter.escapeAndEnclose(dt==null?"null":dt, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_count==null?"null":"" + gmv_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_amount==null?"null":gmv_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(gmv_payment==null?"null":gmv_payment.toPlainString(), delimiters));
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
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_count = null; } else {
      this.gmv_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_amount = null; } else {
      this.gmv_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_payment = null; } else {
      this.gmv_payment = new java.math.BigDecimal(__cur_str);
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
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_count = null; } else {
      this.gmv_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_amount = null; } else {
      this.gmv_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.gmv_payment = null; } else {
      this.gmv_payment = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  public Object clone() throws CloneNotSupportedException {
    ads_gmv_sum_day o = (ads_gmv_sum_day) super.clone();
    return o;
  }

  public void clone0(ads_gmv_sum_day o) throws CloneNotSupportedException {
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("gmv_count", this.gmv_count);
    __sqoop$field_map.put("gmv_amount", this.gmv_amount);
    __sqoop$field_map.put("gmv_payment", this.gmv_payment);
    return __sqoop$field_map;
  }

  public void getFieldMap0(Map<String, Object> __sqoop$field_map) {
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("gmv_count", this.gmv_count);
    __sqoop$field_map.put("gmv_amount", this.gmv_amount);
    __sqoop$field_map.put("gmv_payment", this.gmv_payment);
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("dt".equals(__fieldName)) {
      this.dt = (String) __fieldVal;
    }
    else    if ("gmv_count".equals(__fieldName)) {
      this.gmv_count = (Long) __fieldVal;
    }
    else    if ("gmv_amount".equals(__fieldName)) {
      this.gmv_amount = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("gmv_payment".equals(__fieldName)) {
      this.gmv_payment = (java.math.BigDecimal) __fieldVal;
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
    else    if ("gmv_count".equals(__fieldName)) {
      this.gmv_count = (Long) __fieldVal;
      return true;
    }
    else    if ("gmv_amount".equals(__fieldName)) {
      this.gmv_amount = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("gmv_payment".equals(__fieldName)) {
      this.gmv_payment = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else {
      return false;    }
  }
}
