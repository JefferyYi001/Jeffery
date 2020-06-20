// ORM class for table 'ads_uv_count'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Wed Apr 15 00:37:37 CST 2020
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

public class ads_uv_count extends SqoopRecord  implements DBWritable, Writable {
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
  public ads_uv_count with_dt(String dt) {
    this.dt = dt;
    return this;
  }
  private Long day_count;
  public Long get_day_count() {
    return day_count;
  }
  public void set_day_count(Long day_count) {
    this.day_count = day_count;
  }
  public ads_uv_count with_day_count(Long day_count) {
    this.day_count = day_count;
    return this;
  }
  private Long wk_count;
  public Long get_wk_count() {
    return wk_count;
  }
  public void set_wk_count(Long wk_count) {
    this.wk_count = wk_count;
  }
  public ads_uv_count with_wk_count(Long wk_count) {
    this.wk_count = wk_count;
    return this;
  }
  private Long mn_count;
  public Long get_mn_count() {
    return mn_count;
  }
  public void set_mn_count(Long mn_count) {
    this.mn_count = mn_count;
  }
  public ads_uv_count with_mn_count(Long mn_count) {
    this.mn_count = mn_count;
    return this;
  }
  private String is_weekend;
  public String get_is_weekend() {
    return is_weekend;
  }
  public void set_is_weekend(String is_weekend) {
    this.is_weekend = is_weekend;
  }
  public ads_uv_count with_is_weekend(String is_weekend) {
    this.is_weekend = is_weekend;
    return this;
  }
  private String is_monthend;
  public String get_is_monthend() {
    return is_monthend;
  }
  public void set_is_monthend(String is_monthend) {
    this.is_monthend = is_monthend;
  }
  public ads_uv_count with_is_monthend(String is_monthend) {
    this.is_monthend = is_monthend;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_uv_count)) {
      return false;
    }
    ads_uv_count that = (ads_uv_count) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.day_count == null ? that.day_count == null : this.day_count.equals(that.day_count));
    equal = equal && (this.wk_count == null ? that.wk_count == null : this.wk_count.equals(that.wk_count));
    equal = equal && (this.mn_count == null ? that.mn_count == null : this.mn_count.equals(that.mn_count));
    equal = equal && (this.is_weekend == null ? that.is_weekend == null : this.is_weekend.equals(that.is_weekend));
    equal = equal && (this.is_monthend == null ? that.is_monthend == null : this.is_monthend.equals(that.is_monthend));
    return equal;
  }
  public boolean equals0(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ads_uv_count)) {
      return false;
    }
    ads_uv_count that = (ads_uv_count) o;
    boolean equal = true;
    equal = equal && (this.dt == null ? that.dt == null : this.dt.equals(that.dt));
    equal = equal && (this.day_count == null ? that.day_count == null : this.day_count.equals(that.day_count));
    equal = equal && (this.wk_count == null ? that.wk_count == null : this.wk_count.equals(that.wk_count));
    equal = equal && (this.mn_count == null ? that.mn_count == null : this.mn_count.equals(that.mn_count));
    equal = equal && (this.is_weekend == null ? that.is_weekend == null : this.is_weekend.equals(that.is_weekend));
    equal = equal && (this.is_monthend == null ? that.is_monthend == null : this.is_monthend.equals(that.is_monthend));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.day_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.wk_count = JdbcWritableBridge.readLong(3, __dbResults);
    this.mn_count = JdbcWritableBridge.readLong(4, __dbResults);
    this.is_weekend = JdbcWritableBridge.readString(5, __dbResults);
    this.is_monthend = JdbcWritableBridge.readString(6, __dbResults);
  }
  public void readFields0(ResultSet __dbResults) throws SQLException {
    this.dt = JdbcWritableBridge.readString(1, __dbResults);
    this.day_count = JdbcWritableBridge.readLong(2, __dbResults);
    this.wk_count = JdbcWritableBridge.readLong(3, __dbResults);
    this.mn_count = JdbcWritableBridge.readLong(4, __dbResults);
    this.is_weekend = JdbcWritableBridge.readString(5, __dbResults);
    this.is_monthend = JdbcWritableBridge.readString(6, __dbResults);
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
    JdbcWritableBridge.writeLong(day_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(wk_count, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(mn_count, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeString(is_weekend, 5 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeString(is_monthend, 6 + __off, 12, __dbStmt);
    return 6;
  }
  public void write0(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(dt, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(day_count, 2 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(wk_count, 3 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeLong(mn_count, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeString(is_weekend, 5 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeString(is_monthend, 6 + __off, 12, __dbStmt);
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
        this.day_count = null;
    } else {
    this.day_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.wk_count = null;
    } else {
    this.wk_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.mn_count = null;
    } else {
    this.mn_count = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.is_weekend = null;
    } else {
    this.is_weekend = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.is_monthend = null;
    } else {
    this.is_monthend = Text.readString(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.day_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_count);
    }
    if (null == this.wk_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.wk_count);
    }
    if (null == this.mn_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.mn_count);
    }
    if (null == this.is_weekend) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, is_weekend);
    }
    if (null == this.is_monthend) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, is_monthend);
    }
  }
  public void write0(DataOutput __dataOut) throws IOException {
    if (null == this.dt) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dt);
    }
    if (null == this.day_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.day_count);
    }
    if (null == this.wk_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.wk_count);
    }
    if (null == this.mn_count) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.mn_count);
    }
    if (null == this.is_weekend) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, is_weekend);
    }
    if (null == this.is_monthend) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, is_monthend);
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
    __sb.append(FieldFormatter.escapeAndEnclose(day_count==null?"null":"" + day_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(wk_count==null?"null":"" + wk_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(mn_count==null?"null":"" + mn_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(is_weekend==null?"null":is_weekend, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(is_monthend==null?"null":is_monthend, delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  public void toString0(DelimiterSet delimiters, StringBuilder __sb, char fieldDelim) {
    __sb.append(FieldFormatter.escapeAndEnclose(dt==null?"null":dt, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(day_count==null?"null":"" + day_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(wk_count==null?"null":"" + wk_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(mn_count==null?"null":"" + mn_count, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(is_weekend==null?"null":is_weekend, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(is_monthend==null?"null":is_monthend, delimiters));
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
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_count = null; } else {
      this.day_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.wk_count = null; } else {
      this.wk_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.mn_count = null; } else {
      this.mn_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.is_weekend = null; } else {
      this.is_weekend = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.is_monthend = null; } else {
      this.is_monthend = __cur_str;
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
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.day_count = null; } else {
      this.day_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.wk_count = null; } else {
      this.wk_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N") || __cur_str.length() == 0) { this.mn_count = null; } else {
      this.mn_count = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.is_weekend = null; } else {
      this.is_weekend = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("\\N")) { this.is_monthend = null; } else {
      this.is_monthend = __cur_str;
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  public Object clone() throws CloneNotSupportedException {
    ads_uv_count o = (ads_uv_count) super.clone();
    return o;
  }

  public void clone0(ads_uv_count o) throws CloneNotSupportedException {
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("day_count", this.day_count);
    __sqoop$field_map.put("wk_count", this.wk_count);
    __sqoop$field_map.put("mn_count", this.mn_count);
    __sqoop$field_map.put("is_weekend", this.is_weekend);
    __sqoop$field_map.put("is_monthend", this.is_monthend);
    return __sqoop$field_map;
  }

  public void getFieldMap0(Map<String, Object> __sqoop$field_map) {
    __sqoop$field_map.put("dt", this.dt);
    __sqoop$field_map.put("day_count", this.day_count);
    __sqoop$field_map.put("wk_count", this.wk_count);
    __sqoop$field_map.put("mn_count", this.mn_count);
    __sqoop$field_map.put("is_weekend", this.is_weekend);
    __sqoop$field_map.put("is_monthend", this.is_monthend);
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("dt".equals(__fieldName)) {
      this.dt = (String) __fieldVal;
    }
    else    if ("day_count".equals(__fieldName)) {
      this.day_count = (Long) __fieldVal;
    }
    else    if ("wk_count".equals(__fieldName)) {
      this.wk_count = (Long) __fieldVal;
    }
    else    if ("mn_count".equals(__fieldName)) {
      this.mn_count = (Long) __fieldVal;
    }
    else    if ("is_weekend".equals(__fieldName)) {
      this.is_weekend = (String) __fieldVal;
    }
    else    if ("is_monthend".equals(__fieldName)) {
      this.is_monthend = (String) __fieldVal;
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
    else    if ("day_count".equals(__fieldName)) {
      this.day_count = (Long) __fieldVal;
      return true;
    }
    else    if ("wk_count".equals(__fieldName)) {
      this.wk_count = (Long) __fieldVal;
      return true;
    }
    else    if ("mn_count".equals(__fieldName)) {
      this.mn_count = (Long) __fieldVal;
      return true;
    }
    else    if ("is_weekend".equals(__fieldName)) {
      this.is_weekend = (String) __fieldVal;
      return true;
    }
    else    if ("is_monthend".equals(__fieldName)) {
      this.is_monthend = (String) __fieldVal;
      return true;
    }
    else {
      return false;    }
  }
}
