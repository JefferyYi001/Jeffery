// ORM class for table 'null'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Wed Apr 01 00:04:29 CST 2020
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

public class QueryResult extends SqoopRecord  implements DBWritable, Writable {
  private final int PROTOCOL_VERSION = 3;
  public int getClassFormatVersion() { return PROTOCOL_VERSION; }
  protected ResultSet __cur_result_set;
  private Long id;
  public Long get_id() {
    return id;
  }
  public void set_id(Long id) {
    this.id = id;
  }
  public QueryResult with_id(Long id) {
    this.id = id;
    return this;
  }
  private java.math.BigDecimal final_total_amount;
  public java.math.BigDecimal get_final_total_amount() {
    return final_total_amount;
  }
  public void set_final_total_amount(java.math.BigDecimal final_total_amount) {
    this.final_total_amount = final_total_amount;
  }
  public QueryResult with_final_total_amount(java.math.BigDecimal final_total_amount) {
    this.final_total_amount = final_total_amount;
    return this;
  }
  private String order_status;
  public String get_order_status() {
    return order_status;
  }
  public void set_order_status(String order_status) {
    this.order_status = order_status;
  }
  public QueryResult with_order_status(String order_status) {
    this.order_status = order_status;
    return this;
  }
  private Long user_id;
  public Long get_user_id() {
    return user_id;
  }
  public void set_user_id(Long user_id) {
    this.user_id = user_id;
  }
  public QueryResult with_user_id(Long user_id) {
    this.user_id = user_id;
    return this;
  }
  private String out_trade_no;
  public String get_out_trade_no() {
    return out_trade_no;
  }
  public void set_out_trade_no(String out_trade_no) {
    this.out_trade_no = out_trade_no;
  }
  public QueryResult with_out_trade_no(String out_trade_no) {
    this.out_trade_no = out_trade_no;
    return this;
  }
  private java.sql.Timestamp create_time;
  public java.sql.Timestamp get_create_time() {
    return create_time;
  }
  public void set_create_time(java.sql.Timestamp create_time) {
    this.create_time = create_time;
  }
  public QueryResult with_create_time(java.sql.Timestamp create_time) {
    this.create_time = create_time;
    return this;
  }
  private java.sql.Timestamp operate_time;
  public java.sql.Timestamp get_operate_time() {
    return operate_time;
  }
  public void set_operate_time(java.sql.Timestamp operate_time) {
    this.operate_time = operate_time;
  }
  public QueryResult with_operate_time(java.sql.Timestamp operate_time) {
    this.operate_time = operate_time;
    return this;
  }
  private Integer province_id;
  public Integer get_province_id() {
    return province_id;
  }
  public void set_province_id(Integer province_id) {
    this.province_id = province_id;
  }
  public QueryResult with_province_id(Integer province_id) {
    this.province_id = province_id;
    return this;
  }
  private java.math.BigDecimal benefit_reduce_amount;
  public java.math.BigDecimal get_benefit_reduce_amount() {
    return benefit_reduce_amount;
  }
  public void set_benefit_reduce_amount(java.math.BigDecimal benefit_reduce_amount) {
    this.benefit_reduce_amount = benefit_reduce_amount;
  }
  public QueryResult with_benefit_reduce_amount(java.math.BigDecimal benefit_reduce_amount) {
    this.benefit_reduce_amount = benefit_reduce_amount;
    return this;
  }
  private java.math.BigDecimal original_total_amount;
  public java.math.BigDecimal get_original_total_amount() {
    return original_total_amount;
  }
  public void set_original_total_amount(java.math.BigDecimal original_total_amount) {
    this.original_total_amount = original_total_amount;
  }
  public QueryResult with_original_total_amount(java.math.BigDecimal original_total_amount) {
    this.original_total_amount = original_total_amount;
    return this;
  }
  private java.math.BigDecimal feight_fee;
  public java.math.BigDecimal get_feight_fee() {
    return feight_fee;
  }
  public void set_feight_fee(java.math.BigDecimal feight_fee) {
    this.feight_fee = feight_fee;
  }
  public QueryResult with_feight_fee(java.math.BigDecimal feight_fee) {
    this.feight_fee = feight_fee;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QueryResult)) {
      return false;
    }
    QueryResult that = (QueryResult) o;
    boolean equal = true;
    equal = equal && (this.id == null ? that.id == null : this.id.equals(that.id));
    equal = equal && (this.final_total_amount == null ? that.final_total_amount == null : this.final_total_amount.equals(that.final_total_amount));
    equal = equal && (this.order_status == null ? that.order_status == null : this.order_status.equals(that.order_status));
    equal = equal && (this.user_id == null ? that.user_id == null : this.user_id.equals(that.user_id));
    equal = equal && (this.out_trade_no == null ? that.out_trade_no == null : this.out_trade_no.equals(that.out_trade_no));
    equal = equal && (this.create_time == null ? that.create_time == null : this.create_time.equals(that.create_time));
    equal = equal && (this.operate_time == null ? that.operate_time == null : this.operate_time.equals(that.operate_time));
    equal = equal && (this.province_id == null ? that.province_id == null : this.province_id.equals(that.province_id));
    equal = equal && (this.benefit_reduce_amount == null ? that.benefit_reduce_amount == null : this.benefit_reduce_amount.equals(that.benefit_reduce_amount));
    equal = equal && (this.original_total_amount == null ? that.original_total_amount == null : this.original_total_amount.equals(that.original_total_amount));
    equal = equal && (this.feight_fee == null ? that.feight_fee == null : this.feight_fee.equals(that.feight_fee));
    return equal;
  }
  public boolean equals0(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof QueryResult)) {
      return false;
    }
    QueryResult that = (QueryResult) o;
    boolean equal = true;
    equal = equal && (this.id == null ? that.id == null : this.id.equals(that.id));
    equal = equal && (this.final_total_amount == null ? that.final_total_amount == null : this.final_total_amount.equals(that.final_total_amount));
    equal = equal && (this.order_status == null ? that.order_status == null : this.order_status.equals(that.order_status));
    equal = equal && (this.user_id == null ? that.user_id == null : this.user_id.equals(that.user_id));
    equal = equal && (this.out_trade_no == null ? that.out_trade_no == null : this.out_trade_no.equals(that.out_trade_no));
    equal = equal && (this.create_time == null ? that.create_time == null : this.create_time.equals(that.create_time));
    equal = equal && (this.operate_time == null ? that.operate_time == null : this.operate_time.equals(that.operate_time));
    equal = equal && (this.province_id == null ? that.province_id == null : this.province_id.equals(that.province_id));
    equal = equal && (this.benefit_reduce_amount == null ? that.benefit_reduce_amount == null : this.benefit_reduce_amount.equals(that.benefit_reduce_amount));
    equal = equal && (this.original_total_amount == null ? that.original_total_amount == null : this.original_total_amount.equals(that.original_total_amount));
    equal = equal && (this.feight_fee == null ? that.feight_fee == null : this.feight_fee.equals(that.feight_fee));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.id = JdbcWritableBridge.readLong(1, __dbResults);
    this.final_total_amount = JdbcWritableBridge.readBigDecimal(2, __dbResults);
    this.order_status = JdbcWritableBridge.readString(3, __dbResults);
    this.user_id = JdbcWritableBridge.readLong(4, __dbResults);
    this.out_trade_no = JdbcWritableBridge.readString(5, __dbResults);
    this.create_time = JdbcWritableBridge.readTimestamp(6, __dbResults);
    this.operate_time = JdbcWritableBridge.readTimestamp(7, __dbResults);
    this.province_id = JdbcWritableBridge.readInteger(8, __dbResults);
    this.benefit_reduce_amount = JdbcWritableBridge.readBigDecimal(9, __dbResults);
    this.original_total_amount = JdbcWritableBridge.readBigDecimal(10, __dbResults);
    this.feight_fee = JdbcWritableBridge.readBigDecimal(11, __dbResults);
  }
  public void readFields0(ResultSet __dbResults) throws SQLException {
    this.id = JdbcWritableBridge.readLong(1, __dbResults);
    this.final_total_amount = JdbcWritableBridge.readBigDecimal(2, __dbResults);
    this.order_status = JdbcWritableBridge.readString(3, __dbResults);
    this.user_id = JdbcWritableBridge.readLong(4, __dbResults);
    this.out_trade_no = JdbcWritableBridge.readString(5, __dbResults);
    this.create_time = JdbcWritableBridge.readTimestamp(6, __dbResults);
    this.operate_time = JdbcWritableBridge.readTimestamp(7, __dbResults);
    this.province_id = JdbcWritableBridge.readInteger(8, __dbResults);
    this.benefit_reduce_amount = JdbcWritableBridge.readBigDecimal(9, __dbResults);
    this.original_total_amount = JdbcWritableBridge.readBigDecimal(10, __dbResults);
    this.feight_fee = JdbcWritableBridge.readBigDecimal(11, __dbResults);
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
    JdbcWritableBridge.writeLong(id, 1 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(final_total_amount, 2 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeString(order_status, 3 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(user_id, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeString(out_trade_no, 5 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeTimestamp(create_time, 6 + __off, 93, __dbStmt);
    JdbcWritableBridge.writeTimestamp(operate_time, 7 + __off, 93, __dbStmt);
    JdbcWritableBridge.writeInteger(province_id, 8 + __off, 4, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(benefit_reduce_amount, 9 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(original_total_amount, 10 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(feight_fee, 11 + __off, 3, __dbStmt);
    return 11;
  }
  public void write0(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeLong(id, 1 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(final_total_amount, 2 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeString(order_status, 3 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeLong(user_id, 4 + __off, -5, __dbStmt);
    JdbcWritableBridge.writeString(out_trade_no, 5 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeTimestamp(create_time, 6 + __off, 93, __dbStmt);
    JdbcWritableBridge.writeTimestamp(operate_time, 7 + __off, 93, __dbStmt);
    JdbcWritableBridge.writeInteger(province_id, 8 + __off, 4, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(benefit_reduce_amount, 9 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(original_total_amount, 10 + __off, 3, __dbStmt);
    JdbcWritableBridge.writeBigDecimal(feight_fee, 11 + __off, 3, __dbStmt);
  }
  public void readFields(DataInput __dataIn) throws IOException {
this.readFields0(__dataIn);  }
  public void readFields0(DataInput __dataIn) throws IOException {
    if (__dataIn.readBoolean()) { 
        this.id = null;
    } else {
    this.id = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.final_total_amount = null;
    } else {
    this.final_total_amount = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.order_status = null;
    } else {
    this.order_status = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.user_id = null;
    } else {
    this.user_id = Long.valueOf(__dataIn.readLong());
    }
    if (__dataIn.readBoolean()) { 
        this.out_trade_no = null;
    } else {
    this.out_trade_no = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.create_time = null;
    } else {
    this.create_time = new Timestamp(__dataIn.readLong());
    this.create_time.setNanos(__dataIn.readInt());
    }
    if (__dataIn.readBoolean()) { 
        this.operate_time = null;
    } else {
    this.operate_time = new Timestamp(__dataIn.readLong());
    this.operate_time.setNanos(__dataIn.readInt());
    }
    if (__dataIn.readBoolean()) { 
        this.province_id = null;
    } else {
    this.province_id = Integer.valueOf(__dataIn.readInt());
    }
    if (__dataIn.readBoolean()) { 
        this.benefit_reduce_amount = null;
    } else {
    this.benefit_reduce_amount = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.original_total_amount = null;
    } else {
    this.original_total_amount = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.feight_fee = null;
    } else {
    this.feight_fee = com.cloudera.sqoop.lib.BigDecimalSerializer.readFields(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.id);
    }
    if (null == this.final_total_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.final_total_amount, __dataOut);
    }
    if (null == this.order_status) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, order_status);
    }
    if (null == this.user_id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.user_id);
    }
    if (null == this.out_trade_no) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, out_trade_no);
    }
    if (null == this.create_time) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.create_time.getTime());
    __dataOut.writeInt(this.create_time.getNanos());
    }
    if (null == this.operate_time) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.operate_time.getTime());
    __dataOut.writeInt(this.operate_time.getNanos());
    }
    if (null == this.province_id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeInt(this.province_id);
    }
    if (null == this.benefit_reduce_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.benefit_reduce_amount, __dataOut);
    }
    if (null == this.original_total_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.original_total_amount, __dataOut);
    }
    if (null == this.feight_fee) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.feight_fee, __dataOut);
    }
  }
  public void write0(DataOutput __dataOut) throws IOException {
    if (null == this.id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.id);
    }
    if (null == this.final_total_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.final_total_amount, __dataOut);
    }
    if (null == this.order_status) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, order_status);
    }
    if (null == this.user_id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.user_id);
    }
    if (null == this.out_trade_no) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, out_trade_no);
    }
    if (null == this.create_time) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.create_time.getTime());
    __dataOut.writeInt(this.create_time.getNanos());
    }
    if (null == this.operate_time) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeLong(this.operate_time.getTime());
    __dataOut.writeInt(this.operate_time.getNanos());
    }
    if (null == this.province_id) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeInt(this.province_id);
    }
    if (null == this.benefit_reduce_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.benefit_reduce_amount, __dataOut);
    }
    if (null == this.original_total_amount) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.original_total_amount, __dataOut);
    }
    if (null == this.feight_fee) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    com.cloudera.sqoop.lib.BigDecimalSerializer.write(this.feight_fee, __dataOut);
    }
  }
  private static final DelimiterSet __outputDelimiters = new DelimiterSet((char) 9, (char) 10, (char) 0, (char) 0, false);
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
    __sb.append(FieldFormatter.escapeAndEnclose(id==null?"\\N":"" + id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(final_total_amount==null?"\\N":final_total_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order_status==null?"\\N":order_status, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(user_id==null?"\\N":"" + user_id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(out_trade_no==null?"\\N":out_trade_no, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(create_time==null?"\\N":"" + create_time, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(operate_time==null?"\\N":"" + operate_time, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(province_id==null?"\\N":"" + province_id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(benefit_reduce_amount==null?"\\N":benefit_reduce_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(original_total_amount==null?"\\N":original_total_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(feight_fee==null?"\\N":feight_fee.toPlainString(), delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  public void toString0(DelimiterSet delimiters, StringBuilder __sb, char fieldDelim) {
    __sb.append(FieldFormatter.escapeAndEnclose(id==null?"\\N":"" + id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(final_total_amount==null?"\\N":final_total_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(order_status==null?"\\N":order_status, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(user_id==null?"\\N":"" + user_id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(out_trade_no==null?"\\N":out_trade_no, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(create_time==null?"\\N":"" + create_time, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(operate_time==null?"\\N":"" + operate_time, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(province_id==null?"\\N":"" + province_id, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(benefit_reduce_amount==null?"\\N":benefit_reduce_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(original_total_amount==null?"\\N":original_total_amount.toPlainString(), delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(feight_fee==null?"\\N":feight_fee.toPlainString(), delimiters));
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
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.id = null; } else {
      this.id = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.final_total_amount = null; } else {
      this.final_total_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.order_status = null; } else {
      this.order_status = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.user_id = null; } else {
      this.user_id = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.out_trade_no = null; } else {
      this.out_trade_no = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.create_time = null; } else {
      this.create_time = java.sql.Timestamp.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.operate_time = null; } else {
      this.operate_time = java.sql.Timestamp.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.province_id = null; } else {
      this.province_id = Integer.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.benefit_reduce_amount = null; } else {
      this.benefit_reduce_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.original_total_amount = null; } else {
      this.original_total_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.feight_fee = null; } else {
      this.feight_fee = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  private void __loadFromFields0(Iterator<String> __it) {
    String __cur_str = null;
    try {
    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.id = null; } else {
      this.id = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.final_total_amount = null; } else {
      this.final_total_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.order_status = null; } else {
      this.order_status = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.user_id = null; } else {
      this.user_id = Long.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.out_trade_no = null; } else {
      this.out_trade_no = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.create_time = null; } else {
      this.create_time = java.sql.Timestamp.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.operate_time = null; } else {
      this.operate_time = java.sql.Timestamp.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.province_id = null; } else {
      this.province_id = Integer.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.benefit_reduce_amount = null; } else {
      this.benefit_reduce_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.original_total_amount = null; } else {
      this.original_total_amount = new java.math.BigDecimal(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.feight_fee = null; } else {
      this.feight_fee = new java.math.BigDecimal(__cur_str);
    }

    } catch (RuntimeException e) {    throw new RuntimeException("Can't parse input data: '" + __cur_str + "'", e);    }  }

  public Object clone() throws CloneNotSupportedException {
    QueryResult o = (QueryResult) super.clone();
    o.create_time = (o.create_time != null) ? (java.sql.Timestamp) o.create_time.clone() : null;
    o.operate_time = (o.operate_time != null) ? (java.sql.Timestamp) o.operate_time.clone() : null;
    return o;
  }

  public void clone0(QueryResult o) throws CloneNotSupportedException {
    o.create_time = (o.create_time != null) ? (java.sql.Timestamp) o.create_time.clone() : null;
    o.operate_time = (o.operate_time != null) ? (java.sql.Timestamp) o.operate_time.clone() : null;
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("id", this.id);
    __sqoop$field_map.put("final_total_amount", this.final_total_amount);
    __sqoop$field_map.put("order_status", this.order_status);
    __sqoop$field_map.put("user_id", this.user_id);
    __sqoop$field_map.put("out_trade_no", this.out_trade_no);
    __sqoop$field_map.put("create_time", this.create_time);
    __sqoop$field_map.put("operate_time", this.operate_time);
    __sqoop$field_map.put("province_id", this.province_id);
    __sqoop$field_map.put("benefit_reduce_amount", this.benefit_reduce_amount);
    __sqoop$field_map.put("original_total_amount", this.original_total_amount);
    __sqoop$field_map.put("feight_fee", this.feight_fee);
    return __sqoop$field_map;
  }

  public void getFieldMap0(Map<String, Object> __sqoop$field_map) {
    __sqoop$field_map.put("id", this.id);
    __sqoop$field_map.put("final_total_amount", this.final_total_amount);
    __sqoop$field_map.put("order_status", this.order_status);
    __sqoop$field_map.put("user_id", this.user_id);
    __sqoop$field_map.put("out_trade_no", this.out_trade_no);
    __sqoop$field_map.put("create_time", this.create_time);
    __sqoop$field_map.put("operate_time", this.operate_time);
    __sqoop$field_map.put("province_id", this.province_id);
    __sqoop$field_map.put("benefit_reduce_amount", this.benefit_reduce_amount);
    __sqoop$field_map.put("original_total_amount", this.original_total_amount);
    __sqoop$field_map.put("feight_fee", this.feight_fee);
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("id".equals(__fieldName)) {
      this.id = (Long) __fieldVal;
    }
    else    if ("final_total_amount".equals(__fieldName)) {
      this.final_total_amount = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("order_status".equals(__fieldName)) {
      this.order_status = (String) __fieldVal;
    }
    else    if ("user_id".equals(__fieldName)) {
      this.user_id = (Long) __fieldVal;
    }
    else    if ("out_trade_no".equals(__fieldName)) {
      this.out_trade_no = (String) __fieldVal;
    }
    else    if ("create_time".equals(__fieldName)) {
      this.create_time = (java.sql.Timestamp) __fieldVal;
    }
    else    if ("operate_time".equals(__fieldName)) {
      this.operate_time = (java.sql.Timestamp) __fieldVal;
    }
    else    if ("province_id".equals(__fieldName)) {
      this.province_id = (Integer) __fieldVal;
    }
    else    if ("benefit_reduce_amount".equals(__fieldName)) {
      this.benefit_reduce_amount = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("original_total_amount".equals(__fieldName)) {
      this.original_total_amount = (java.math.BigDecimal) __fieldVal;
    }
    else    if ("feight_fee".equals(__fieldName)) {
      this.feight_fee = (java.math.BigDecimal) __fieldVal;
    }
    else {
      throw new RuntimeException("No such field: " + __fieldName);
    }
  }
  public boolean setField0(String __fieldName, Object __fieldVal) {
    if ("id".equals(__fieldName)) {
      this.id = (Long) __fieldVal;
      return true;
    }
    else    if ("final_total_amount".equals(__fieldName)) {
      this.final_total_amount = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("order_status".equals(__fieldName)) {
      this.order_status = (String) __fieldVal;
      return true;
    }
    else    if ("user_id".equals(__fieldName)) {
      this.user_id = (Long) __fieldVal;
      return true;
    }
    else    if ("out_trade_no".equals(__fieldName)) {
      this.out_trade_no = (String) __fieldVal;
      return true;
    }
    else    if ("create_time".equals(__fieldName)) {
      this.create_time = (java.sql.Timestamp) __fieldVal;
      return true;
    }
    else    if ("operate_time".equals(__fieldName)) {
      this.operate_time = (java.sql.Timestamp) __fieldVal;
      return true;
    }
    else    if ("province_id".equals(__fieldName)) {
      this.province_id = (Integer) __fieldVal;
      return true;
    }
    else    if ("benefit_reduce_amount".equals(__fieldName)) {
      this.benefit_reduce_amount = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("original_total_amount".equals(__fieldName)) {
      this.original_total_amount = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else    if ("feight_fee".equals(__fieldName)) {
      this.feight_fee = (java.math.BigDecimal) __fieldVal;
      return true;
    }
    else {
      return false;    }
  }
}
