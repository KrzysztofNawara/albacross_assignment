package knawara.albacross.event_labeler.types

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.StringType

/**
  * This is container for DataFrame with valid schema
  * Validation is performed in companion object
  * df must have source_ip, which represents IPv6, with all components and all leading 0s inside component
  */
class EventList private (val df: DataFrame)

object EventList {
  val IP_COLUMN_NAME = "source_ip"

  /**
    * Performs validation and, if provided DataFrame has expected schema, returns EventList
    * Input DF's schema must have source_ip: Array[Byte] field, which is non-nullable
    * Byte array is assumed to store binary representation of IPv6 address
    *
    * Also transforms IP field to string
    */
  def apply(dataFrame: DataFrame): EventList = {
    val ipField = dataFrame.schema(IP_COLUMN_NAME)

    if(ipField == null) throw new MissingIpFieldException
    if(ipField.nullable) throw new MissingIpFieldException
    ipField.dataType match {
      case arrType : StringType => "valid"
      case _ => throw new IpFieldIncorrectTypeException
    }

    val dfWithIpTransformed = TypesUtils.addIpFormattingStep(dataFrame, IP_COLUMN_NAME)

    new EventList(dfWithIpTransformed)
  }
}

sealed class EventListException extends RuntimeException
class MissingIpFieldException extends EventListException
class NullableIpFieldException extends EventListException
class IpFieldIncorrectTypeException extends EventListException
