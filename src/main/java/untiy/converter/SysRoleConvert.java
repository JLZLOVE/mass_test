package untiy.converter;

import org.mapstruct.Mapper;
import untiy.entity.SysRole;
import untiy.entity.dto.SysRoleDTO;

@Mapper(componentModel = "spring")
public interface SysRoleConvert {
    /**
     * Entity → DTO（屏蔽敏感字段，这里 idCard 自动忽略）
     */
    SysRoleDTO toDto(SysRole sysRole);
    /**
     * DTO → Entity（新增/更新时用）
     */
    SysRoleDTO toEntity(SysRoleDTO sysRoleDTO);

}
