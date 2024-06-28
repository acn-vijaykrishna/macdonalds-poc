package com.mcd;

import com.mcd.model.Event;
import com.mcd.model.EvCustom;
import com.mcd.model.Info;
import com.mcd.service.AvroConverter;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroConverterTest {

    private static Schema schema;

    @BeforeAll
    static void setup() throws IOException {
        try (InputStream schemaStream = AvroConverterTest.class.getResourceAsStream("/event.avsc")) {
            schema = new Schema.Parser().parse(schemaStream);
        }
    }

    @Test
    void testToAvro() throws IOException {
        AvroConverter converter = new AvroConverter(schema);

        // Create test data
        Info info = new Info();
        info.setCode("3605");
        info.setData("example data");
        EvCustom evCustom = new EvCustom();
        evCustom.setInfo(info);
        Event event = new Event();
        event.setStoreId("25001000");
        event.setRegId("1510");
        event.setTime("20240601122046");
        event.setType("Ev_Custom");  // Make sure to set the "Type" field
        event.setEvCustom(evCustom);

        // Convert to Avro
        byte[] avroData = converter.toAvro(event);


        // Read back the Avro data to verify
        DatumReader<GenericRecord> datumReader = new SpecificDatumReader<>(schema);
        try (SeekableByteArrayInput input = new SeekableByteArrayInput(avroData);
             DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(input, datumReader)) {

            GenericRecord result = dataFileReader.next();
            System.out.println("GenericRecord Result ==>"+result);
            // Verify the fields
            assertEquals(event.getRegId(), result.get("RegId").toString());
            assertEquals(event.getTime(), result.get("Time").toString());
            assertEquals(event.getType(), result.get("Type").toString());
            assertEquals(event.getStoreId(), result.get("storeId").toString());

            GenericRecord evCustomRecord = (GenericRecord) result.get("Ev_Custom");
            GenericRecord infoRecord = (GenericRecord) evCustomRecord.get("Info");

            assertEquals(event.getEvCustom().getInfo().getCode(), infoRecord.get("code").toString());
            assertEquals(event.getEvCustom().getInfo().getData(), infoRecord.get("data").toString());
        }
    }
}
