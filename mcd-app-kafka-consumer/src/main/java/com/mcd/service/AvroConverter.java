package com.mcd.service;

import com.mcd.model.Event;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroConverter {

    private final Schema schema;

    public AvroConverter(Schema schema) {
        this.schema = schema;
    }

    public byte[] toAvro(Event event) throws IOException {
        GenericRecord eventRecord = new GenericData.Record(schema);

        eventRecord.put("RegId", event.getRegId());
        eventRecord.put("Time", event.getTime());
        eventRecord.put("Type", event.getType());
        eventRecord.put("storeId", event.getStoreId());

        // Create Ev_Custom record
        GenericRecord evCustomRecord = new GenericData.Record(schema.getField("Ev_Custom").schema());

        // Create Info record
        GenericRecord infoRecord = new GenericData.Record(schema.getField("Ev_Custom").schema().getField("Info").schema());
        infoRecord.put("code", event.getEvCustom().getInfo().getCode());
        infoRecord.put("data", event.getEvCustom().getInfo().getData());

        evCustomRecord.put("Info", infoRecord);
        eventRecord.put("Ev_Custom", evCustomRecord);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

        dataFileWriter.create(schema, outputStream);
        dataFileWriter.append(eventRecord);
        dataFileWriter.close();

        return outputStream.toByteArray();
    }

}
