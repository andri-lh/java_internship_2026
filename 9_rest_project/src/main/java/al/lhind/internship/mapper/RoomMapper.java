package al.lhind.internship.mapper;

import al.lhind.internship.dto.RoomDto;
import al.lhind.internship.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "hotelId", source = "hotel.id")
    RoomDto toDto(Room room);

    @Mapping(target = "hotel", ignore = true)
    Room toEntity(RoomDto roomDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntityFromDto(RoomDto roomDto, @MappingTarget Room room);
}
