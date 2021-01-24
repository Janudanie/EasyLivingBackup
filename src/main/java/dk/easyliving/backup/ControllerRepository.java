package dk.easyliving.backup;

import dk.easyliving.dto.units.LdrSensor;
import dk.easyliving.dto.units.PirSensor;
import dk.easyliving.dto.units.RelayActivator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import java.util.List;

@Repository
interface LdrRepository extends MongoRepository<LdrSensor,String> {

    public LdrSensor findLdrSensorByMacAdd(String macAdd);

    public void deleteByMacAdd(String macAdd);

    public boolean existsByName(String name);
    public boolean existsByMacAdd(String macAdd);

    public List<LdrSensor> findAll();
}

@Repository
interface PirRepository extends MongoRepository<PirSensor,String>{
    public PirSensor findPirSensorByMacAdd(String macAdd);

    public void deleteByMacAdd(String macAdd);

    public boolean existsByName(String name);
    public boolean existsByMacAdd(String macAdd);

    public List<PirSensor> findAll();
}

@Repository
interface RelayRepository extends MongoRepository<RelayActivator,String>{
    public RelayActivator findRelayActivatorByMacAdd(String macAdd);

    public void deleteByMacAdd(String macAdd);

    public boolean existsByName(String name);
    public boolean existsByMacAdd(String macAdd);

    public List<RelayActivator> findAll();

}
