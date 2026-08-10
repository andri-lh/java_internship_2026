package al.lhind.internship.mapper;

import al.lhind.internship.dto.HotelDto;
import al.lhind.internship.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelDto toDto(Hotel hotel);

    Hotel toEntity(HotelDto hotelDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    void updateEntityFromDto(HotelDto hotelDto, @MappingTarget Hotel hotel);
}
