/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exportsc.app;

import com.exportsc.model.Clientes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.oxm.xstream.XStreamMarshaller;
/**
 *
 * @author Anderson_Rodrigues2
 */

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    public DataSource dataSource;
    
    @Bean
    public DataSource dataSource(){
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://127.0.0.1/springbatch");
        dataSource.setUsername("root");
        dataSource.setPassword("");        
        return dataSource;
    }
    
   @Bean(destroyMethod="")
    public JdbcCursorItemReader<Clientes> reader () {
        JdbcCursorItemReader<Clientes> reader = new JdbcCursorItemReader<Clientes>();
        reader.setDataSource(dataSource);
        reader.setSql(("SELECT id, name, description from clientes"));
        reader.setRowMapper(new ClientesRowMapper());
        return reader;
    }
    
    public class ClientesRowMapper implements RowMapper<Clientes> {

        @Override
        public Clientes mapRow(ResultSet rs, int rowNum) throws SQLException {
            Clientes clientes = new Clientes();
            clientes.setId(rs.getInt("id"));
            clientes.setName(rs.getString("name"));
            clientes.setDescription(rs.getString("description"));

            return clientes;
        } 
    }
   
   @Bean(destroyMethod="")
    public StaxEventItemWriter<Clientes> writer() throws ClassNotFoundException {
        
        StaxEventItemWriter<Clientes> writer = new StaxEventItemWriter<Clientes>();
        writer.setResource(new ClassPathResource("inputs/clientes.xml"));
        
        Map<String, String> aliases = new HashMap<String, String>();
        aliases.put("clientes", "com.exportsc.model.Clientes");
       
        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        
        writer.setMarshaller(marshaller);
        writer.setRootTagName("clientes");
        writer.setOverwriteOutput(true);
        
        return writer;
    }
    
    @Bean
    public Step step1() throws ClassNotFoundException{
        return stepBuilderFactory.get("setp1")
                .<Clientes, Clientes> chunk(10)
                .reader(reader())
                .writer(writer())
                .build();
    }
    
    @Bean
    public Job exportClientesJob() throws ClassNotFoundException{
        return jobBuilderFactory.get("exportClientesJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();
    }
}
