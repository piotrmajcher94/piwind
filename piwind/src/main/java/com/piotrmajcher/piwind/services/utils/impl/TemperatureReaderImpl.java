package com.piotrmajcher.piwind.services.utils.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.piotrmajcher.piwind.services.utils.CommandExecutor;
import com.piotrmajcher.piwind.services.utils.TemperatureReader;
import com.piotrmajcher.piwind.services.utils.exceptions.CommandExecutionException;
import com.piotrmajcher.piwind.services.utils.exceptions.TemperatureReaderException;

@Component
public class TemperatureReaderImpl implements TemperatureReader{

    private static final String FETCH_INTERNAL_TEMP_COMMAND = "python ./scripts/read_internal_temperature.py";
    private static final String FETCH_EXTERNAL_TEMP_COMMAND = "python ./scripts/read_external_temperature.py";
    private static final String FETCHED_NULL_VALUE = "Received null value while trying to fetch the temperature data";

    @Autowired
    private CommandExecutor commandExecutor;

    @Override
    public Double fetchInternalTemperature() throws TemperatureReaderException {
    	return readTemperatureDoubleValue(FETCH_INTERNAL_TEMP_COMMAND);
    }

    @Override
    public Double fetchExternalTemperature() throws TemperatureReaderException {
        return readTemperatureDoubleValue(FETCH_EXTERNAL_TEMP_COMMAND);
    }

    private Double readTemperatureDoubleValue(String command) throws TemperatureReaderException {
        Double temperatureCelsius;
        List<String> commandResultList;
        try {
            commandResultList = commandExecutor.executeCommand(command);

            if (commandResultList == null || commandResultList.size() != 1 || commandResultList.get(0) == null) {
                throw new TemperatureReaderException(FETCHED_NULL_VALUE);
            } else {
                temperatureCelsius = Double.valueOf(commandResultList.get(0));
            }
        } catch (CommandExecutionException | NumberFormatException e) {
            throw new TemperatureReaderException(e.getMessage());
        }
        return temperatureCelsius;
    }
}
