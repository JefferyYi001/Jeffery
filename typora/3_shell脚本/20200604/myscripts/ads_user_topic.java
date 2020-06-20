// ORM class for table 'ads_user_topic'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Wed Apr 15 00:38:22 CST 2020
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

public class ads_user_topic extends SqoopRecord  implements DBWritable, Writable {
  private final int PROTOCOL_VERSION = 3;
  public int getClassFormatVersion() { return PROTOCOL_VERSION; }
  protected ResultSet __cur_result_set;
  private String stat_date;
  public String get_stat_date() {
    return stat_date;
  }
  public void set_stat_date(String stat_date) {
    this.stat_date = stat_date;
  }
  public ads_user_topic with_stat_date(String stat_date) {
    this.stat_date = stat_date;
    return this;
  }
  private Long day_users;
  public Long get_day_users() {
    return day_users;
  }
  public void set_day_users(Long day_users) {
    this.day_users = day_users;
  }
  public ads_user_topic with_day_users(Long day_users) {
    this.day_users = day_users;
    return this;
  }
  private Long day_new_users;
  public Long get_day_new_users() {
    return day_new_users;
  }
  public void set_day_new_users(Long day_new_users) {
    this.day_new_users = day_new_users;
  }
  public ads_user_topic with_day_new_users(Long day_new_users) {
    this.day_new_users = day_new_users;
    return this;
  }
  private Long day_new_payment_users;
  public Long get_day_new_payment_users() {
    return day_new_payment_users;
  }
  public void set_day_new_payment_users(Long day_new_payment_users) {
    this.day_new_payment_users = day_new_payment_users;
  }
  public ads_user_topic with_day_new_payment_users(Long day_new_payment_users) {
    this.day_new_payment_users = day_new_payment_users;
    return this;
  }
  private Long payment_users;
  public Long get_payment_users() {
    return payment_users;
  }
  public void set_payment_users(Long payment_users) {
    this.payment_users = payment_users;
  }
  public ads_user_topic with_payment_users(Long payment_users) {
    this.payment_users = payment_users;
    return this;
  }
  private Long users;
  public Long get_users() {
    return users;
  }
  public void set_users(Long users) {
    this.users = users;
  }
  public ads_user_topic with_users(Long users) {
    this.users = users;
    return this;
  }
  private java.math.BigDecimal day_users2users;
  public java.math.BigDecimal get_day_users2users() {
    return day_users2users;
  }
  public void set_day_users2users(java.math.BigDecimal day_users2users) {
    this.day_users2users = day_users2users;
  }
  public ads_user_topic with_day_users2users(java.math.BigDecimal day_users2users) {
    this.day_users2users = day_users2users;
    return this;
  }
  private java.math.BigDecimal payment_users2users;
  public java.math.BigDecimal get_payment_users2users() {
    return payment_users2users;
  }
  public void set_payment_users2users(java.math.BigDecimal payment_users2users) {
    this.payment_users2users = payment_users2users;
  }
  public ads_user_topic with_payment_users2users(java.math.BigDecimal payment_users2users) {
    this.payment_users2users = payment_users2users;
    return this;
  }
  private java.math.BigDecimal day_new_users2users;
  public java.math.BigDecimal get_day_new_users2users() {
    return day_new_users2users;
  }
  public void set_day_new_users2users(java.math.BigDecimal day_new_users2users) {
    this.day_new_users2users = day_new_users2users;
  }
  public ads_user_topic with_day_new_users2users(java.math.BigDecimal day_new_users2users) {
    this.day_new_users2users = day_new_users2users;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_user_topic)) {
      return false;
    }
    ads_user_topic that = (ads_user_topic) o;
    boolean equal = true;
    equal = equal && (this.stat_date == null ? that.stat_date == null : this.stat_date.equals(that.stat_date));
    equal = equal && (this.day_users == null ? that.day_users == null : this.day_users.equals(that.day_users));
    equal = equal && (this.day_new_users == null ? that.day_new_users == null : this.day_new_users.equals(that.day_new_users));
    equal = equal && (this.day_new_payment_users == null ? that.day_new_payment_users == null : this.day_new_payment_users.equals(that.day_new_payment_users));
    equal = equal && (this.payment_users == null ? that.payment_users == null : this.payment_users.equals(that.payment_users));
    equal = equal && (this.users == null ? that.users == null : this.users.equals(that.users));
    equal = equal && (this.day_users2users == null ? that.day_users2users == null : this.day_users2users.equals(that.day_users2users));
    equal = equal && (this.payment_users2users == null ? that.payment_users2users == null : this.payment_users2users.equals(that.payment_users2users));
    equal = equal && (this.day_new_users2users == null ? that.day_new_users2users == null : this.day_new_users2users.equals(that.day_new_users2users));
    return equal;
  }
  public boolean equals0(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_user_topic)) {
      return false;
    }
    ads_user_topic that = (ads_user_topic) o;
    boolean equal = true;
    equal = equal && (this.stat_date == null ? that.stat_date == null : this.stat_date.equals(that.stat_date));
    equal = equal && (this.day_users == null ? that.day_users == null : this.day_users.equals(that.day_users));
    equal = equal && (this.day_new_users == null ? that.day_new_users == null : this.day_new_users.equals(that.day_new_users));
    equal = equal && (this.day_new_payment_users == null ? that.day_new_payment_users == null : this.day_new_payment_users.equals(that.day_new_payment_users));
    equal = equal && (this.payment_users == null ? that.payment_users == null : this.payment_users.equals(that.payment_users));
    equal = equal && (this.users == null ? that.users == null : this.users.equals(that.users));
    equal = equal && (this.day_users2users == null ? that.day_users2users == null : this.day_users2users.equals(that.day_users2users));
    equal = equal && (this.payment_users2users == null ? that.payment_users2users == null : this.payment_users2users.equals(that.payment_users2users));
    equal = equal && (this.day_new_users2users == null ? that.day_new_users2users == null : this.day_new_users2users.equals(that.day_new_users2users));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.stat_date = JdbcWritableBridge.readString(1, __dbResults);
    this.day_users = JdbcWritableBridge.readLong(2, __dbResults);
    this.day_new_users = JdbcWritableBridge.readLong(3, __dbResults);
    this.day_new_payment_users = JdbcWritableBridge.readLong(4, __dbResults);
    this.payment_users = JdbcWritableBridge.readLong(5, __dbResults);
    this.users = JdbcWritableBridge.readLong(6, __dbResults);
    this.day_users2users = JdbcWritableBridge.readBigDecimal(7, __dbResults);
    this.payment_users2users = JdbcWritableBridge.readBigDecimal(8, __dbResults);
    this.day_new_users2users = JdbcWritableBridge.readBigDecimal(9, __dbResults);
  }
  public void readFields0(ResultSet __dbResults) throws SQLException {
    this.stat_date = JdbcWritableBridge.readString(1, __dbResults);
    this.day_users = JdbcWritableBridge.readLong(2, __dbResults);
    this.day_new_users = JdbcWritableBridge.readLong(3, __dbResults);
    this.day_new_payment_users = JdbcWritableBridge.readLong(4, __dbResults);
    this.payment_users = JdbcWritableBridge.readLong(5, __dbResults);
    this.users = JdbcWritableBridge.readLong(6, __dbResults);
    this.day_users2users = JdbcWritableBridge.readBigDecimal(7, __dbResults);
    this.payment_users2users = JdbcWritableBridge.readBigDecimal(8, __dbResults);
    this.day_new_users2users = JdbcWritableBridge.readBigDecimal(9, __dbResults);
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
    JdbcWritableBridge.writeString(stat_date, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(day_users, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(day_new_users, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(day_new_payment_users, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(payment_users, 5 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(users, 6 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(day_users2users, 7 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(payment_users2users, 8 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(day_new_users2users, 9 + __off, 3, __dbStmt);
    return 9;
  }
  public void write0(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(stat_date, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(day_users, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(day_new_users, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(day_new_payment_users, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(payment_users, 5 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(users, 6 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(day_users2users, 7 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(payment_users2users, 8 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(day_new_users2users, 9 + __off, 3, __dbStmt);
  }
  public void readFields(DataInput __dataIn) throws IOException {
this.readFields0(__dataIn);  }
  public void readFields0(DataInput __dataIn) throws IOException {
    if (__dataIn.readBoolean()) { 
        this.stat_date = null;
    } else {
    this.stat_date = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.day_users = null;
    } else {
    this.day_users = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.day_new_users = null;
    } else {
    this.day_new_users = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.day_new_payment_users = null;
    } else {
    this.day_new_payment_users = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.payment_users = null;
    } else {
    this.payment_users = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.users = null;
    } else {
    this.users = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.day_users2users = null;
    } else {
    this.day_users2users = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.payment_users2users = null;
    } else {
    this.payment_users2users = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.day_new_users2users = null;
    } else {
    this.day_new_users2users = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.stat_date) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, stat_date);
    }
    if (null == this.day_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_users);
    }
    if (null == this.day_new_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_new_users);
    }
    if (null == this.day_new_payment_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_new_payment_users);
    }
    if (null == this.payment_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.payment_users);
    }
    if (null == this.users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.users);
    }
    if (null == this.day_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.day_users2users, __dataOut);
    }
    if (null == this.payment_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.payment_users2users, __dataOut);
    }
    if (null == this.day_new_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.day_new_users2users, __dataOut);
    }
  }
  public void write0(DataOutput __dataOut) throws IOException {
    if (null == this.stat_date) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, stat_date);
    }
    if (null == this.day_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_users);
    }
    if (null == this.day_new_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_new_users);
    }
    if (null == this.day_new_payment_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_new_payment_users);
    }
    if (null == this.payment_users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.payment_users);
    }
    if (null == this.users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.users);
    }
    if (null == this.day_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.day_users2users, __dataOut);
    }
    if (null == this.payment_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.payment_users2users, __dataOut);
    }
    if (null == this.day_new_users2users) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.day_new_users2users, __dataOut);
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
    __sb.append(FieldFormatter.escapeAndEnclose(stat_date==null?"null":stat_date, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_users==null?"null":"" + day_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_users==null?"null":"" + day_new_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_payment_users==null?"null":"" + day_new_payment_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_users==null?"null":"" + payment_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(users==null?"null":"" + users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_users2users==null?"null":day_users2users.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_users2users==null?"null":payment_users2users.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_users2users==null?"null":day_new_users2users.toPlainString(), delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  public void toString0(DelimiterSet delimiters, StringBuilder __sb, char fieldDelim) {
    __sb.append(FieldFormatter.escapeAndEnclose(stat_date==null?"null":stat_date, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_users==null?"null":"" + day_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_users==null?"null":"" + day_new_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_payment_users==null?"null":"" + day_new_payment_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_users==null?"null":"" + payment_users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(users==null?"null":"" + users, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_users2users==null?"null":day_users2users.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(payment_users2users==null?"null":payment_users2users.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_new_users2users==null?"null":day_new_users2users.toPlainString(), delimiters));
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
    if (__cur_str.equals("\\N")) { this.stat_date = null; } else {
      this.stat_date = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_users = null; } else {
      this.day_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_users = null; } else {
      this.day_new_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_payment_users = null; } else {
      this.day_new_payment_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_users = null; } else {
      this.payment_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.users = null; } else {
      this.users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_users2users = null; } else {
      this.day_users2users = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_users2users = null; } else {
      this.payment_users2users = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_users2users = null; } else {
      this.day_new_users2users = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  private void __loadFromFields0(Iterator<String> __it) {
    String __cur_str = null;
    try {
    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.stat_date = null; } else {
      this.stat_date = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_users = null; } else {
      this.day_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_users = null; } else {
      this.day_new_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_payment_users = null; } else {
      this.day_new_payment_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_users = null; } else {
      this.payment_users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.users = null; } else {
      this.users = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_users2users = null; } else {
      this.day_users2users = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.payment_users2users = null; } else {
      this.payment_users2users = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_new_users2users = null; } else {
      this.day_new_users2users = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  public Object clone() throws CloneNotSupportedException {
    ads_user_topic o = (ads_user_topic) super.clone();
    return o;
  }

  public void clone0(ads_user_topic o) throws CloneNotSupportedException {
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("stat_date", this.stat_date);
    __sqoop$field_map.put("day_users", this.day_users);
    __sqoop$field_map.put("day_new_users", this.day_new_users);
    __sqoop$field_map.put("day_new_payment_users", this.day_new_payment_users);
    __sqoop$field_map.put("payment_users", this.payment_users);
    __sqoop$field_map.put("users", this.users);
    __sqoop$field_map.put("day_users2users", this.day_users2users);
    __sqoop$field_map.put("payment_users2users", this.payment_users2users);
    __sqoop$field_map.put("day_new_users2users", this.day_new_users2users);
    return __sqoop$field_map;
  }

  public void getFieldMap0(Map<String, Object> __sqoop$field_map) {
    __sqoop$field_map.put("stat_date", this.stat_date);
    __sqoop$field_map.put("day_users", this.day_users);
    __sqoop$field_map.put("day_new_users", this.day_new_users);
    __sqoop$field_map.put("day_new_payment_users", this.day_new_payment_users);
    __sqoop$field_map.put("payment_users", this.payment_users);
    __sqoop$field_map.put("users", this.users);
    __sqoop$field_map.put("day_users2users", this.day_users2users);
    __sqoop$field_map.put("payment_users2users", this.payment_users2users);
    __sqoop$field_map.put("day_new_users2users", this.day_new_users2users);
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("stat_date".equals(__fieldName)) {
      this.stat_date = (String) __fieldVal;
    }
    else    if ("day_users".equals(__fieldName)) {
      this.day_users = (Long) __fieldVal;
    }
    else    if ("day_new_users".equals(__fieldName)) {
      this.day_new_users = (Long) __fieldVal;
    }
    else    if ("day_new_payment_users".equals(__fieldName)) {
      this.day_new_payment_users = (Long) __fieldVal;
    }
    else    if ("payment_users".equals(__fieldName)) {
      this.payment_users = (Long) __fieldVal;
    }
    else    if ("users".equals(__fieldName)) {
      this.users = (Long) __fieldVal;
    }
    else    if ("day_users2users".equals(__fieldName)) {
      this.day_users2users = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("payment_users2users".equals(__fieldName)) {
      this.payment_users2users = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("day_new_users2users".equals(__fieldName)) {
      this.day_new_users2users = (java.math.BigDecimal) __fieldVal;
    }
    else {
      throw new RuntimeException("No such field: " + __fieldName);
    }
  }
  public boolean setField0(String __fieldName, Object __fieldVal) {
    if ("stat_date".equals(__fieldName)) {
      this.stat_date = (String) __fieldVal;
      return true;
    }
    else    if ("day_users".equals(__fieldName)) {
      this.day_users = (Long) __fieldVal;
      return true;
    }
    else    if ("day_new_users".equals(__fieldName)) {
      this.day_new_users = (Long) __fieldVal;
      return true;
    }
    else    if ("day_new_payment_users".equals(__fieldName)) {
      this.day_new_payment_users = (Long) __fieldVal;
      return true;
    }
    else    if ("payment_users".equals(__fieldName)) {
      this.payment_users = (Long) __fieldVal;
      return true;
    }
    else    if ("users".equals(__fieldName)) {
      this.users = (Long) __fieldVal;
      return true;
    }
    else    if ("day_users2users".equals(__fieldName)) {
      this.day_users2users = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("payment_users2users".equals(__fieldName)) {
      this.payment_users2users = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("day_new_users2users".equals(__fieldName)) {
      this.day_new_users2users = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else {
      return false;    }
  }
}
