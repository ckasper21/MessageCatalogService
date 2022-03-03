package com.kasper.MessageCatalogService;

import com.kasper.MessageCatalogService.protos.Event;
import com.kasper.MessageCatalogService.protos.Execution;
import io.nats.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

@SpringBootApplication
public class MessageCatalogServiceApplication {

	Logger log = LoggerFactory.getLogger(this.getClass().getName());

	BufferedWriter writerExecution;
	BufferedWriter writerEvent;
	Connection connection;

	@PostConstruct
	public void runTaskOnStartup() throws IOException, InterruptedException {
		// Setup NATS connection
		Options o = new Options.Builder().server(Options.DEFAULT_URL).maxReconnects(-1).build();
		connection = Nats.connect();

		// Setup writers for storing messages
		writerExecution = new BufferedWriter(new FileWriter("./logs/executions.csv", true));
		writerEvent = new BufferedWriter(new FileWriter("./logs/events.csv", true));
	}

	public static void main(String[] args) {
		SpringApplication.run(MessageCatalogServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunnerExecution () {
		return args -> {
			Dispatcher dispatcher = connection.createDispatcher(message -> {
				try {
					Execution.execution e = Execution.execution.parseFrom(message.getData());
					log.info("Received execution message: [" + e.getSymbol() + "," + e.getMarket() + "," + e.getPrice() +
							"," + e.getQuantity() + "," + e.getExecutionEpoch() + "," + e.getStateSymbol() + "]");
					writeToStorageExecutions(e);
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			});
			dispatcher.subscribe("execution");
		};
	}

	@Bean
	CommandLineRunner commandLineRunnerEvent () {
		return args -> {
			Dispatcher dispatcher = connection.createDispatcher(message -> {
				try {
					Event.event e = Event.event.parseFrom(message.getData());
					log.info("Received sport_event message: [" + e.getSport() + "," + e.getMatchTitle() + "," + e.getDataEvent() + "]");
					writeToStorageEvents(e);
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			});
			dispatcher.subscribe("sport_event");
		};
	}

	public void writeToStorageExecutions(Execution.execution e) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		try {
			writerExecution.write(t.toString() + "," + e.getSymbol() + "," + e.getMarket() + "," + e.getPrice() +
					"," + e.getQuantity() + "," + e.getExecutionEpoch() + "," + e.getStateSymbol() +"\n");
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}

	public void writeToStorageEvents(Event.event e) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		try {
			writerEvent.write(t.toString() + "," + e.getSport() + "," + e.getMatchTitle() + "," +
					e.getDataEvent() + "\n");
		} catch (IOException ex) {
			log.error(ex.getMessage());
		}
	}

	@PreDestroy
	public void preDestroy() throws IOException, InterruptedException {
		// Close connection and writers
		connection.close();
		writerExecution.close();
		writerEvent.close();
	}
}
