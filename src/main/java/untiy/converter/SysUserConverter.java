package untiy.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import untiy.entity.dto.SysUserDTO ;
import untiy.entity.SysUser;

@Mapper(componentModel = "spring")
public interface SysUserConverter {

    /**
     * Entity → DTO（屏蔽敏感字段，这里 idCard 自动忽略）
     */
    SysUserDTO toDto(SysUser entity);

    /**
     * DTO → Entity（新增/更新时用）
     */
    SysUser toEntity(SysUserDTO dto);


    SysUser selectByUsername(String name);
}