#ifndef RD_CPP_CORE_CPP_UTIL_H
#define RD_CPP_CORE_CPP_UTIL_H

#include "erase_if.h"
#include "std/hash.h"
#include "std/to_string.h"
#include "thirdparty.hpp"
#include "types/wrapper.h"
#include "util/gen_util.h"
#include "util/overloaded.h"
#include "util/shared_function.h"

#include <atomic>
#include <cassert>
#include <iostream>
#include <memory>
#include <sstream>
#include <string>
#include <thread>

#define RD_ASSERT_MSG(expr, msg)                      \
	if (!(expr))                                      \
	{                                                 \
		std::cerr << std::endl << (msg) << std::endl; \
		assert(expr);                                 \
	}
#define RD_ASSERT_THROW_MSG(expr, msg)                \
	if (!(expr))                                      \
	{                                                 \
		std::cerr << std::endl << (msg) << std::endl; \
		throw std::runtime_error(msg);                \
	}

namespace rd
{
namespace wrapper
{
template <typename T>
struct TransparentKeyEqual
{
	using is_transparent = void;

	bool operator()(T const& val_l, T const& val_r) const
	{
		return val_l == val_r;
	}

	bool operator()(Wrapper<T> const& ptr_l, Wrapper<T> const& ptr_r) const
	{
		return ptr_l == ptr_r;
	}

	bool operator()(T const* val_l, T const* val_r) const
	{
		return *val_l == *val_r;
	}

	bool operator()(T const& val_r, Wrapper<T> const& ptr_l) const
	{
		return *ptr_l == val_r;
	}

	bool operator()(T const& val_l, T const* ptr_r) const
	{
		return val_l == *ptr_r;
	}

	bool operator()(Wrapper<T> const& val_l, T const* ptr_r) const
	{
		return *val_l == *ptr_r;
	}
};

template <typename T>
struct TransparentHash
{
	using is_transparent = void;
	using transparent_key_equal = std::equal_to<>;

	size_t operator()(T const& val) const noexcept
	{
		return rd::hash<T>()(val);
	}

	size_t operator()(Wrapper<T> const& ptr) const noexcept
	{
		return rd::hash<Wrapper<T>>()(ptr);
	}

	size_t operator()(T const* val) const noexcept
	{
		return rd::hash<T>()(*val);
	}
};
}	 // namespace wrapper
}	 // namespace rd

#endif	  // RD_CPP_CORE_CPP_UTIL_H
