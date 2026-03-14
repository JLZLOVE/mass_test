package untiy.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
@Mapper
public interface CommonMapper {
    List<String> requireMapper(HashMap<String, Object> map);
}
