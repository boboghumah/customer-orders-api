# customer-orders-api

## API Notes

### Date Format

All dates use `yyyy-MM-dd` (example: `2024-06-30`).

### Pagination

List endpoints accept standard Spring Data pagination query parameters:

- `page` (0-based page index, default 0)
- `size` (page size, default 20)
- `sort` (sorting, example: `sort=orderDate,desc`)

Responses include paging metadata such as `size`, `number`, `totalElements`, and `totalPages`.
